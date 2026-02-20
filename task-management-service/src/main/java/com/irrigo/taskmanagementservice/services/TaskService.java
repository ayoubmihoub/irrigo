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

    /**
     * Récupère toutes les tâches d'irrigation.
     */
    public List<IrrigationTask> getAllTasks() {
        return taskRepository.findAll();
    }

    /**
     * Récupère une tâche spécifique par son identifiant.
     * Utilisée par le contrôleur pour l'affichage détaillé ou avant une modification.
     */
    public IrrigationTask getTaskById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tâche d'irrigation non trouvée avec l'ID : " + id));
    }

    /**
     * Enregistre une nouvelle tâche.
     * Le statut sera "planned" par défaut grâce à la configuration de l'entité.
     */
    public IrrigationTask saveTask(IrrigationTask task) {
        // Si le statut n'est pas fourni par le frontend/Postman
        if (task.getStatus() == null) {
            task.setStatus(ETaskStatus.planned);
        }
        return taskRepository.save(task);
    }

    /**
     * Met à jour l'intégralité des données d'une tâche existante.
     */
    public IrrigationTask updateTask(Long id, IrrigationTask taskDetails) {
        // On récupère la tâche existante ou on lève une exception
        IrrigationTask task = getTaskById(id);

        // Mise à jour de tous les champs caractéristiques
        task.setName(taskDetails.getName());
        task.setLocation(taskDetails.getLocation());
        task.setDuration(taskDetails.getDuration());
        task.setWaterAmount(taskDetails.getWaterAmount());
        task.setDebit(taskDetails.getDebit());
        task.setStartTime(taskDetails.getStartTime());
        task.setCrop(taskDetails.getCrop());

        // Mise à jour du statut (planned, ongoing, ou terminated)
        task.setStatus(taskDetails.getStatus());

        return taskRepository.save(task);
    }

    /**
     * Supprime une tâche après avoir vérifié son existence.
     */
    public void deleteTask(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new RuntimeException("Impossible de supprimer : Tâche non trouvée");
        }
        taskRepository.deleteById(id);
    }
}