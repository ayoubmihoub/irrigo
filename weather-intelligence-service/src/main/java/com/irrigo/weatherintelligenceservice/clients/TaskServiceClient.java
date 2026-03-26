package com.irrigo.weatherintelligenceservice.clients;

import com.irrigo.taskmanagementservice.dto.TaskDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "task-management-service")
public interface TaskServiceClient {
    @GetMapping("/api/tasks/all")
    List<TaskDTO> getAllTasks();

    @GetMapping("/api/tasks/{id}")
    TaskDTO getTaskById(@PathVariable("id") Long id);

    // AJOUT : Récupérer le cumul d'eau (7 derniers jours) pour le bilan hydrique
    @GetMapping("/api/tasks/history/sum")
    Double getWaterHistory(
            @RequestParam("location") String location,
            @RequestParam("crop") String crop,
            @RequestParam("days") int days
    );
}