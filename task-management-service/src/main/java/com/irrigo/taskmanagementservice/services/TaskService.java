package com.irrigo.taskmanagementservice.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.irrigo.taskmanagementservice.dto.TaskDTO;
import com.irrigo.taskmanagementservice.entities.ETaskStatus;
import com.irrigo.taskmanagementservice.entities.IrrigationTask;
import com.irrigo.taskmanagementservice.repositories.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Récupère l'email de l'utilisateur authentifié.
     */
    private String getCurrentUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    /**
     * Vérifie si l'utilisateur est ADMIN.
     */
    private boolean isAdmin() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    // --- CONSULTATION ET RAPPORTS ---

    public List<IrrigationTask> getAllTasks() {
        if (isAdmin()) return taskRepository.findAll();
        return taskRepository.findByUserEmail(getCurrentUserEmail());
    }

    public IrrigationTask getTaskById(Long id) {
        IrrigationTask task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tâche non trouvée avec l'ID : " + id));

        if (!isAdmin() && !task.getUserEmail().equals(getCurrentUserEmail())) {
            throw new RuntimeException("Accès refusé");
        }
        return task;
    }

    public List<IrrigationTask> getTasksByUserAfter(String email, LocalDateTime date) {
        return taskRepository.findByUserEmailAndStartTimeAfter(email, date);
    }

    public List<String> getUniqueCrops() {
        if (isAdmin()) return taskRepository.findDistinctCrops();
        return taskRepository.findDistinctCropsByUser(getCurrentUserEmail());
    }

    public Double getWaterHistorySum(String location, String crop, int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        Double sum = taskRepository.sumWaterAmount(location, crop, since);
        return (sum != null) ? sum : 0.0;
    }

    // --- GESTION DES TÂCHES (CRUD) ---

    public IrrigationTask saveTask(TaskDTO dto) {
        IrrigationTask task = convertToEntity(dto);
        task.setUserEmail(getCurrentUserEmail());

        if (task.getStatus() == null) {
            task.setStatus(ETaskStatus.planned);
        }
        return taskRepository.save(task);
    }

    public IrrigationTask updateTask(Long id, TaskDTO details) {
        IrrigationTask task = getTaskById(id);

        task.setName(details.getName());
        task.setLocation(details.getLocation());
        task.setDuration(details.getDuration());
        task.setWaterAmount(details.getWaterAmount());
        task.setDebit(details.getDebit());
        task.setStartTime(details.getStartTime());
        task.setCrop(details.getCrop());
        task.setSurface(details.getSurface());
        task.setPlantingDate(details.getPlantingDate());
        task.setSoilProfile(details.getSoilProfile() != null ?
                com.irrigo.taskmanagementservice.entities.ESoilType.valueOf(details.getSoilProfile()) : null);

        if (details.getStatus() != null) {
            task.setStatus(ETaskStatus.valueOf(details.getStatus()));
        }

        try {
            task.setParcelJson(objectMapper.writeValueAsString(details.getParcel()));
            task.setSubParcelsJson(objectMapper.writeValueAsString(details.getSubParcels()));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Erreur de traitement GeoJSON");
        }

        return taskRepository.save(task);
    }

    public void deleteTask(Long id) {
        taskRepository.delete(getTaskById(id));
    }

    // --- AUTOMATISATION ---

    @Scheduled(fixedRate = 60000)
    public void updateTaskStatusesAutomatically() {
        LocalDateTime now = LocalDateTime.now();

        // Passage de planned à ongoing (Sans le statut cancelled pour le moment)
        List<IrrigationTask> toStart = taskRepository.findByStatusAndStartTimeBefore(ETaskStatus.planned, now);
        for (IrrigationTask task : toStart) {
            task.setStatus(ETaskStatus.ongoing);
            taskRepository.save(task);
        }

        // Passage de ongoing à terminated
        List<IrrigationTask> ongoingTasks = taskRepository.findByStatus(ETaskStatus.ongoing);
        for (IrrigationTask task : ongoingTasks) {
            if (now.isAfter(task.getStartTime().plusMinutes(task.getDuration()))) {
                task.setStatus(ETaskStatus.terminated);
                taskRepository.save(task);
            }
        }
    }

    // --- NOTIFICATIONS ---

    public List<String> scanAndGetNotifications(String email) {
        List<IrrigationTask> userTasks = taskRepository.findByUserEmail(email);
        LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);
        List<String> messages = new ArrayList<>();

        for (IrrigationTask task : userTasks) {
            if (task.getStartTime() == null) continue;

            LocalDateTime taskStart = task.getStartTime().withSecond(0).withNano(0);
            LocalDateTime taskEnd = taskStart.plusMinutes(task.getDuration());

            if (now.isAfter(taskEnd) && now.isBefore(taskEnd.plusDays(7))) {
                messages.add("La tâche '" + task.getName() + "' est terminée (" + formatEventDate(taskEnd) + ").");
            }
            else if ((now.isEqual(taskStart) || now.isAfter(taskStart)) && now.isBefore(taskStart.plusDays(7))) {
                messages.add("La tâche '" + task.getName() + "' commence maintenant (" + formatEventDate(taskStart) + ").");
            }
            else if ((now.isEqual(taskStart.minusMinutes(15)) || now.isAfter(taskStart.minusMinutes(15))) && now.isBefore(taskStart)) {
                messages.add("La tâche '" + task.getName() + "' va commencer dans 15 minutes (" + formatEventDate(taskStart) + ").");
            }
        }
        return messages;
    }

    private String formatEventDate(LocalDateTime dateTime) {
        LocalDateTime now = LocalDateTime.now();
        if (dateTime.toLocalDate().equals(now.toLocalDate())) {
            return "aujourd'hui à " + dateTime.format(DateTimeFormatter.ofPattern("HH:mm"));
        } else {
            return dateTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));
        }
    }

    // --- MAPPING INTERNE ---

    private IrrigationTask convertToEntity(TaskDTO dto) {
        IrrigationTask entity = IrrigationTask.builder()
                .name(dto.getName())
                .location(dto.getLocation())
                .surface(dto.getSurface())
                .duration(dto.getDuration())
                .waterAmount(dto.getWaterAmount())
                .debit(dto.getDebit())
                .startTime(dto.getStartTime())
                .crop(dto.getCrop())
                .plantingDate(dto.getPlantingDate())
                .build();

        try {
            entity.setParcelJson(objectMapper.writeValueAsString(dto.getParcel()));
            entity.setSubParcelsJson(objectMapper.writeValueAsString(dto.getSubParcels()));
        } catch (JsonProcessingException e) {
            entity.setParcelJson("{}");
            entity.setSubParcelsJson("[]");
        }
        return entity;
    }
}