package com.irrigo.taskmanagementservice.controllers;

import com.irrigo.taskmanagementservice.dto.TaskDTO;
import com.irrigo.taskmanagementservice.entities.IrrigationTask;
import com.irrigo.taskmanagementservice.services.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    /**
     * Scanne les notifications pour l'utilisateur connecté.
     * L'email est extrait du token JWT via le SecurityContext.
     */
    @GetMapping("/notifications/scan")
    public ResponseEntity<List<String>> scanNotifications() {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(taskService.scanAndGetNotifications(currentUserEmail));
    }

    /**
     * Récupère l'historique des tâches des 30 derniers jours pour un email spécifique.
     */
    @GetMapping("/user/{email}/recent")
    public ResponseEntity<List<IrrigationTask>> getRecentTasks(@PathVariable String email) {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        return ResponseEntity.ok(taskService.getTasksByUserAfter(email, thirtyDaysAgo));
    }

    /**
     * Somme de la consommation d'eau historique.
     */
    @GetMapping("/history/sum")
    public ResponseEntity<Double> getWaterHistorySum(
            @RequestParam String location,
            @RequestParam String crop,
            @RequestParam int days) {
        return ResponseEntity.ok(taskService.getWaterHistorySum(location, crop, days));
    }

    @GetMapping("/crops/unique")
    public ResponseEntity<List<String>> getUniqueCrops() {
        return ResponseEntity.ok(taskService.getUniqueCrops());
    }

    @GetMapping("/all")
    public ResponseEntity<List<IrrigationTask>> getAllTasks() {
        return ResponseEntity.ok(taskService.getAllTasks());
    }

    @GetMapping("/{id}")
    public ResponseEntity<IrrigationTask> getTaskById(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.getTaskById(id));
    }

    /**
     * Création d'une tâche.
     * Utilise TaskDTO pour supporter les données complexes de la carte (GeoJSON).
     */
    @PostMapping("/create")
    public ResponseEntity<IrrigationTask> createTask(@RequestBody TaskDTO taskDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.saveTask(taskDto));
    }

    /**
     * Mise à jour d'une tâche.
     * Utilise TaskDTO pour inclure les nouveaux champs (surface, soilProfile, etc.).
     */
    @PutMapping("/{id}")
    public ResponseEntity<IrrigationTask> updateTask(@PathVariable Long id, @RequestBody TaskDTO details) {
        return ResponseEntity.ok(taskService.updateTask(id, details));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.ok("Tâche supprimée avec succès !");
    }
}