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
import java.util.List;
import java.util.ArrayList;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private String getCurrentUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private boolean isAdmin() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    // --- LECTURE ---

    public List<IrrigationTask> getAllTasks() {
        if (isAdmin()) return taskRepository.findAll();
        return taskRepository.findByUserEmail(getCurrentUserEmail());
    }

    public IrrigationTask getTaskById(Long id) {
        IrrigationTask task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tâche non trouvée"));
        if (!isAdmin() && !task.getUserEmail().equals(getCurrentUserEmail())) {
            throw new RuntimeException("Accès refusé");
        }
        return task;
    }

    // --- GESTION (CRUD) ---

    public IrrigationTask saveTask(TaskDTO dto) {
        IrrigationTask task = convertToEntity(dto);
        task.setUserEmail(getCurrentUserEmail());

        // La tâche commence immédiatement
        task.setStatus(ETaskStatus.ongoing);
        if (task.getStartTime() == null) {
            task.setStartTime(LocalDateTime.now());
        }

        return taskRepository.save(task);
    }

    public IrrigationTask updateTask(Long id, TaskDTO details) {
        IrrigationTask task = getTaskById(id);
        task.setName(details.getName());
        task.setDuration(details.getDuration());
        task.setStartTime(details.getStartTime());

        if (details.getStatus() != null) {
            task.setStatus(ETaskStatus.valueOf(details.getStatus().toLowerCase()));
        }

        try {
            task.setParcelJson(objectMapper.writeValueAsString(details.getParcel()));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Erreur GeoJSON");
        }

        return taskRepository.save(task);
    }

    public void deleteTask(Long id) {
        taskRepository.delete(getTaskById(id));
    }

    // --- AUTOMATISATION ---

    @Scheduled(fixedRate = 60000) // Vérification toutes les minutes
    public void updateTaskStatusesAutomatically() {
        LocalDateTime now = LocalDateTime.now();

        // Passage de ongoing à terminated basé sur la durée
        List<IrrigationTask> ongoingTasks = taskRepository.findByStatus(ETaskStatus.ongoing);
        for (IrrigationTask task : ongoingTasks) {
            if (task.getStartTime() != null &&
                    now.isAfter(task.getStartTime().plusMinutes(task.getDuration()))) {
                task.setStatus(ETaskStatus.terminated);
                taskRepository.save(task);
            }
        }
    }

    private IrrigationTask convertToEntity(TaskDTO dto) {
        return IrrigationTask.builder()
                .name(dto.getName())
                .location(dto.getLocation())
                .surface(dto.getSurface())
                .duration(dto.getDuration())
                .waterAmount(dto.getWaterAmount())
                .debit(dto.getDebit())
                .startTime(dto.getStartTime())
                .crop(dto.getCrop())
                .build();
    }



    /**
     * Retourne les tâches d'un utilisateur après une date donnée.
     * Requis pour le Report Service.
     */
    public List<IrrigationTask> getTasksByUserAfter(String email, LocalDateTime date) {
        return taskRepository.findByUserEmailAndStartTimeAfter(email, date);
    }

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
    public Double getWaterHistorySum(String location, String crop, int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        Double sum = taskRepository.sumWaterAmount(location, crop, since);
        return (sum != null) ? sum : 0.0;
    }

    public List<String> getUniqueCrops() {
        if (isAdmin()) return taskRepository.findDistinctCrops();
        return taskRepository.findDistinctCropsByUser(getCurrentUserEmail());
    }
}