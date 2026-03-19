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
     * Si l'utilisateur est ADMIN, il voit TOUT. Sinon, seulement les siennes.
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

        // Sécurité : l'admin peut tout voir, l'utilisateur seulement ses tâches
        if (!isAdmin() && !task.getUserEmail().equals(getCurrentUserEmail())) {
            throw new RuntimeException("Accès refusé : vous n'êtes pas propriétaire de cette tâche");
        }
        return task;
    }

    /**
     * Enregistre une nouvelle tâche en y attachant l'email du créateur.
     */
    public IrrigationTask saveTask(IrrigationTask task) {
        task.setUserEmail(getCurrentUserEmail()); // Attachement automatique de l'utilisateur
        if (task.getStatus() == null) {
            task.setStatus(ETaskStatus.planned);
        }
        return taskRepository.save(task);
    }

    /**
     * Met à jour une tâche existante.
     */
    public IrrigationTask updateTask(Long id, IrrigationTask taskDetails) {
        IrrigationTask task = getTaskById(id); // Vérifie déjà les droits d'accès
        task.setName(taskDetails.getName());
        task.setLocation(taskDetails.getLocation());
        task.setDuration(taskDetails.getDuration());
        task.setWaterAmount(taskDetails.getWaterAmount());
        task.setDebit(taskDetails.getDebit());
        task.setSurface(taskDetails.getSurface()); // Champ pour les analytics
        task.setStartTime(taskDetails.getStartTime());
        task.setCrop(taskDetails.getCrop());
        task.setStatus(taskDetails.getStatus());
        return taskRepository.save(task);
    }

    /**
     * Supprime une tâche.
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
            System.out.println("[AUTO] Tâche démarrée : " + task.getName());
        }

        // 2. Passage de 'ongoing' à 'terminated'
        List<IrrigationTask> ongoingTasks = taskRepository.findByStatus(ETaskStatus.ongoing);
        for (IrrigationTask task : ongoingTasks) {
            LocalDateTime endTime = task.getStartTime().plusMinutes(task.getDuration());
            if (now.isAfter(endTime)) {
                task.setStatus(ETaskStatus.terminated);
                taskRepository.save(task);
                System.out.println("[AUTO] Tâche terminée : " + task.getName());
            }
        }
    }
}