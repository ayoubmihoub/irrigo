package com.irrigo.taskmanagementservice.services;

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

    private String getCurrentUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private boolean isAdmin() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
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

    public IrrigationTask saveTask(IrrigationTask task) {
        task.setUserEmail(getCurrentUserEmail());
        if (task.getStatus() == null) task.setStatus(ETaskStatus.planned);
        return taskRepository.save(task);
    }

    public IrrigationTask updateTask(Long id, IrrigationTask details) {
        IrrigationTask task = getTaskById(id);
        task.setName(details.getName());
        task.setLocation(details.getLocation());
        task.setDuration(details.getDuration());
        task.setWaterAmount(details.getWaterAmount());
        task.setDebit(details.getDebit());
        task.setStartTime(details.getStartTime());
        task.setCrop(details.getCrop());
        task.setStatus(details.getStatus());
        task.setSurface(details.getSurface()); // Assurez-vous que ce champ existe dans l'entité
        return taskRepository.save(task);
    }

    public void deleteTask(Long id) {
        taskRepository.delete(getTaskById(id));
    }

    @Scheduled(fixedRate = 60000)
    public void updateTaskStatusesAutomatically() {
        LocalDateTime now = LocalDateTime.now();
        List<IrrigationTask> toStart = taskRepository.findByStatusAndStartTimeBefore(ETaskStatus.planned, now);
        for (IrrigationTask task : toStart) {
            task.setStatus(ETaskStatus.ongoing);
            taskRepository.save(task);
        }
        List<IrrigationTask> ongoingTasks = taskRepository.findByStatus(ETaskStatus.ongoing);
        for (IrrigationTask task : ongoingTasks) {
            if (now.isAfter(task.getStartTime().plusMinutes(task.getDuration()))) {
                task.setStatus(ETaskStatus.terminated);
                taskRepository.save(task);
            }
        }
    }
}