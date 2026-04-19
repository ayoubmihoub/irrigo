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
import java.util.List;

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
}