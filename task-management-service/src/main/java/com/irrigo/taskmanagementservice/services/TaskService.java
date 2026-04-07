package com.irrigo.taskmanagementservice.services;

import com.irrigo.taskmanagementservice.entities.ETaskStatus;
import com.irrigo.taskmanagementservice.entities.IrrigationTask;
import com.irrigo.taskmanagementservice.repositories.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    /**
     * Scanne les tâches et génère des messages individuels.
     * Chaque message disparaît automatiquement après 15 minutes.
     */
    public List<String> scanAndGetNotifications(String email) {
        List<IrrigationTask> userTasks = taskRepository.findByUserEmail(email);
        LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);
        List<String> messages = new ArrayList<>();

        for (IrrigationTask task : userTasks) {
            if (task.getStartTime() == null) continue;

            LocalDateTime taskStart = task.getStartTime().withSecond(0).withNano(0);
            LocalDateTime taskEnd = taskStart.plusMinutes(task.getDuration());

            // 1. Détection : Tâche terminée
            // Affichée uniquement si la fin a eu lieu il y a MOINS de 15 minutes
            if (now.isAfter(taskEnd) && now.isBefore(taskEnd.plusMinutes(16))) {
                messages.add("La tâche '" + task.getName() + "' est terminée.");
            }

            // 2. Détection : Tâche qui commence
            // Affichée depuis l'heure de début jusqu'à 15 minutes après
            else if ((now.isEqual(taskStart) || now.isAfter(taskStart)) && now.isBefore(taskStart.plusMinutes(16))) {
                messages.add("La tâche '" + task.getName() + "' commence maintenant.");
            }

            // 3. Détection : Tâche qui commence dans 15 minutes
            // Affichée uniquement pendant les 15 minutes précédant le début
            else if ((now.isEqual(taskStart.minusMinutes(15)) || now.isAfter(taskStart.minusMinutes(15))) && now.isBefore(taskStart)) {
                messages.add("La tâche '" + task.getName() + "' va commencer dans 15 minutes.");
            }
        }
        return messages;
    }

    // --- MÉTHODES DE GESTION DES TÂCHES (EXISTANTES) ---

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
        task.setDuration(details.getDuration());
        task.setStartTime(details.getStartTime());
        task.setStatus(details.getStatus());
        // ... (autres champs)
        return taskRepository.save(task);
    }

    public void deleteTask(Long id) {
        taskRepository.delete(getTaskById(id));
    }

    /**
     * Mise à jour automatique des statuts en base (toujours utile pour l'historique)
     */
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