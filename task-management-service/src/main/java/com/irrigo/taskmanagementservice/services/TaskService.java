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

    private String getCurrentUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    public List<String> getUniqueCrops() {
        return taskRepository.findDistinctCropsByUser(getCurrentUserEmail());
    }

    public List<IrrigationTask> getAllTasks() {
        return taskRepository.findByUserEmail(getCurrentUserEmail());
    }

    public IrrigationTask getTaskById(Long id) {
        IrrigationTask task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tâche non trouvée"));

        if (!task.getUserEmail().equals(getCurrentUserEmail())) {
            throw new RuntimeException("Accès refusé à cette tâche");
        }
        return task;
    }

    public IrrigationTask saveTask(IrrigationTask task) {
        task.setUserEmail(getCurrentUserEmail());
        if (task.getStatus() == null) {
            task.setStatus(ETaskStatus.planned);
        }
        return taskRepository.save(task);
    }

    public IrrigationTask updateTask(Long id, IrrigationTask taskDetails) {
        IrrigationTask task = getTaskById(id);
        task.setName(taskDetails.getName());
        task.setLocation(taskDetails.getLocation());
        task.setDuration(taskDetails.getDuration());
        task.setWaterAmount(taskDetails.getWaterAmount());
        task.setDebit(taskDetails.getDebit());
        task.setStartTime(taskDetails.getStartTime());
        task.setCrop(taskDetails.getCrop());
        task.setStatus(taskDetails.getStatus());
        return taskRepository.save(task);
    }

    public void deleteTask(Long id) {
        IrrigationTask task = getTaskById(id);
        taskRepository.delete(task);
    }

    /**
     * LOGIQUE AUTOMATIQUE : Mise à jour des statuts en arrière-plan.
     * S'exécute toutes les 60 secondes (60000 ms).
     */
    @Scheduled(fixedRate = 60000)
    public void updateTaskStatusesAutomatically() {
        LocalDateTime now = LocalDateTime.now();

        // 1. Passage de 'planned' à 'ongoing'
        // Si maintenant >= startTime
        List<IrrigationTask> toStart = taskRepository.findByStatusAndStartTimeBefore(ETaskStatus.planned, now);
        for (IrrigationTask task : toStart) {
            task.setStatus(ETaskStatus.ongoing);
            taskRepository.save(task);
            System.out.println("[AUTOMATIQUE] La tâche '" + task.getName() + "' vient de démarrer.");
        }

        // 2. Passage de 'ongoing' à 'terminated'
        // Si maintenant > (startTime + duration)
        List<IrrigationTask> ongoingTasks = taskRepository.findByStatus(ETaskStatus.ongoing);
        for (IrrigationTask task : ongoingTasks) {
            LocalDateTime endTime = task.getStartTime().plusMinutes(task.getDuration());

            if (now.isAfter(endTime)) {
                task.setStatus(ETaskStatus.terminated);
                taskRepository.save(task);
                System.out.println("[AUTOMATIQUE] La tâche '" + task.getName() + "' est terminée.");
            }
        }
    }
}