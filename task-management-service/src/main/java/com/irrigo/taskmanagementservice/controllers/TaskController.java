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

    // ENDPOINT CRUCIAL POUR L'IA : Somme de l'historique
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
    public ResponseEntity<?> getTaskById(@PathVariable Long id) {
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
        return ResponseEntity.ok("Supprimé");
    }
}