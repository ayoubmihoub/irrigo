package com.irrigo.taskmanagementservice.services;

import com.irrigo.taskmanagementservice.entities.ETaskStatus;
import com.irrigo.taskmanagementservice.entities.IrrigationTask;
import com.irrigo.taskmanagementservice.repositories.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    // --- NOUVELLE MÉTHODE POUR LE SERVICE MÉTÉO ---
    public List<String> getUniqueCrops() {
        return taskRepository.findDistinctCrops();
    }

    public List<IrrigationTask> getAllTasks() {
        return taskRepository.findAll();
    }

    public IrrigationTask getTaskById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tâche d'irrigation non trouvée avec l'ID : " + id));
    }

    public IrrigationTask saveTask(IrrigationTask task) {
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
        if (!taskRepository.existsById(id)) {
            throw new RuntimeException("Impossible de supprimer : Tâche non trouvée");
        }
        taskRepository.deleteById(id);
    }
}