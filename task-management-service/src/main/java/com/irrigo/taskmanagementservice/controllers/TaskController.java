package com.irrigo.taskmanagementservice.controllers;

import com.irrigo.taskmanagementservice.entities.IrrigationTask;
import com.irrigo.taskmanagementservice.services.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @GetMapping("/notifications/scan")
    public ResponseEntity<List<String>> scanNotifications() {
        // On ne demande plus l'email en paramètre (@RequestParam).
        // On le récupère directement depuis le contexte de sécurité (rempli par ton filtre JWT)
        String currentUserEmail = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getName();

        return ResponseEntity.ok(taskService.scanAndGetNotifications(currentUserEmail));
    }

    @GetMapping("/user/{email}/recent")
    public ResponseEntity<List<IrrigationTask>> getRecentTasks(@PathVariable String email) {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        // Appel au service pour récupérer l'historique de 30 jours
        return ResponseEntity.ok(taskService.getTasksByUserAfter(email, thirtyDaysAgo));
    }

    @GetMapping("/history/sum")
    public ResponseEntity<Double> getWaterHistorySum(@RequestParam String location, @RequestParam String crop, @RequestParam int days) {
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

    @PostMapping("/create")
    public ResponseEntity<IrrigationTask> createTask(@RequestBody IrrigationTask task) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.saveTask(task));
    }

    @PutMapping("/{id}")
    public ResponseEntity<IrrigationTask> updateTask(@PathVariable Long id, @RequestBody IrrigationTask details) {
        return ResponseEntity.ok(taskService.updateTask(id, details));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.ok("Tâche supprimée avec succès !");
    }
}