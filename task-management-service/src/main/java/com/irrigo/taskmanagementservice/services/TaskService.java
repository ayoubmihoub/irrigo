package com.irrigo.taskmanagementservice.services;

import com.irrigo.taskmanagementservice.entities.ETaskStatus;
import com.irrigo.taskmanagementservice.entities.IrrigationTask;
import com.irrigo.taskmanagementservice.repositories.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    /**
     * Récupère l'email de l'utilisateur actuellement connecté via le JWT.
     */
    private String getCurrentUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    /**
     * Vérifie si l'utilisateur actuel possède le rôle ADMIN.
     */
    private boolean isAdmin() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    /**
     * Calcule le volume d'eau total versé sur X jours.
     * Utilisé par le Weather-Intelligence-Service pour l'IA.
     */
    public Double getWaterHistorySum(String location, String crop, int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        Double sum = taskRepository.sumWaterAmount(location, crop, since);
        return (sum != null) ? sum : 0.0;
    }

    /**
     * Récupère les cultures uniques (filtrées par utilisateur ou globales pour l'admin).
     */
    public List<String> getUniqueCrops() {
        if (isAdmin()) {
            return taskRepository.findDistinctCrops();
        }
        return taskRepository.findDistinctCropsByUser(getCurrentUserEmail());
    }

    /**
     * Récupère toutes les tâches.
     */
    public List<IrrigationTask> getAllTasks() {
        if (isAdmin()) {
            return taskRepository.findAll();
        }
        return taskRepository.findByUserEmail(getCurrentUserEmail());
    }

    /**
     * Récupère une tâche par ID avec vérification de propriété (sauf pour l'admin).
     */
    public IrrigationTask getTaskById(Long id) {
        IrrigationTask task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tâche d'irrigation non trouvée avec l'ID : " + id));

        if (!isAdmin() && !task.getUserEmail().equals(getCurrentUserEmail())) {
            throw new RuntimeException("Accès refusé : vous n'êtes pas propriétaire de cette tâche");
        }
        return task;
    }

    /**
     * Enregistre une nouvelle tâche.
     */
    public IrrigationTask saveTask(IrrigationTask task) {
        task.setUserEmail(getCurrentUserEmail());
        if (task.getStatus() == null) {
            task.setStatus(ETaskStatus.planned);
        }
        return taskRepository.save(task);
    }

    /**
     * Met à jour une tâche existante.
     */
    public IrrigationTask updateTask(Long id, IrrigationTask details) {
        IrrigationTask task = getTaskById(id);
        task.setName(details.getName());
        task.setLocation(details.getLocation());
        task.setDuration(details.getDuration());
        task.setWaterAmount(details.getWaterAmount());
        task.setDebit(details.getDebit());
        task.setSurface(details.getSurface());
        task.setStartTime(details.getStartTime());
        task.setCrop(details.getCrop());
        task.setSoilProfile(details.getSoilProfile());
        task.setPlantingDate(details.getPlantingDate());
        task.setStatus(details.getStatus());
        return taskRepository.save(task);
    }

    /**
     * MÉTHODE AJOUTÉE : Supprime une tâche.
     */
    public void deleteTask(Long id) {
        IrrigationTask task = getTaskById(id); // Vérifie déjà les droits d'accès
        taskRepository.delete(task);
    }

    /**
     * LOGIQUE AUTOMATIQUE : Mise à jour des statuts en arrière-plan (toutes les 60s).
     */
    @Scheduled(fixedRate = 60000)
    public void updateTaskStatusesAutomatically() {
        LocalDateTime now = LocalDateTime.now();

        // 1. Passage de 'planned' à 'ongoing'
        List<IrrigationTask> toStart = taskRepository.findByStatusAndStartTimeBefore(ETaskStatus.planned, now);
        for (IrrigationTask task : toStart) {
            task.setStatus(ETaskStatus.ongoing);
            taskRepository.save(task);
        }

        // 2. Passage de 'ongoing' à 'terminated'
        List<IrrigationTask> ongoingTasks = taskRepository.findByStatus(ETaskStatus.ongoing);
        for (IrrigationTask task : ongoingTasks) {
            LocalDateTime endTime = task.getStartTime().plusMinutes(task.getDuration());
            if (now.isAfter(endTime)) {
                task.setStatus(ETaskStatus.terminated);
                taskRepository.save(task);
            }
        }
    }
}