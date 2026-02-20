package com.irrigo.taskmanagementservice.controllers;

import com.irrigo.taskmanagementservice.entities.IrrigationTask;
import com.irrigo.taskmanagementservice.services.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    // Récupérer toutes les tâches pour les afficher dans les cards
    @GetMapping("/all")
    public ResponseEntity<List<IrrigationTask>> getAllTasks() {
        return ResponseEntity.ok(taskService.getAllTasks());
    }

    // Récupérer une tâche spécifique par ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getTaskById(@PathVariable Long id) {
        try {
            IrrigationTask task = taskService.getTaskById(id);
            return ResponseEntity.ok(task);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Erreur : Tâche d'irrigation non trouvée");
        }
    }

    // Créer une nouvelle tâche (le statut sera "planned" par défaut)
    @PostMapping("/create")
    public ResponseEntity<?> createTask(@RequestBody IrrigationTask task) {
        try {
            IrrigationTask savedTask = taskService.saveTask(task);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedTask);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur : Impossible de créer la tâche");
        }
    }

    // Mettre à jour les données d'une tâche (nom, location, status, etc.)
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTask(@PathVariable Long id, @RequestBody IrrigationTask taskDetails) {
        try {
            IrrigationTask updatedTask = taskService.updateTask(id, taskDetails);
            return ResponseEntity.ok(updatedTask);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Erreur : Impossible de mettre à jour, tâche non trouvée");
        }
    }

    // Supprimer une tâche
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable Long id) {
        try {
            taskService.deleteTask(id);
            return ResponseEntity.ok().body("Tâche d'irrigation supprimée avec succès !");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Erreur : Tâche non trouvée");
        }
    }
}