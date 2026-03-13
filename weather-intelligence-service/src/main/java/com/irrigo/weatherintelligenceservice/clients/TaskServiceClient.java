package com.irrigo.weatherintelligenceservice.clients;

import com.irrigo.taskmanagementservice.dto.TaskDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "task-management-service")
public interface TaskServiceClient {
    @GetMapping("/api/tasks/all")
    List<TaskDTO> getAllTasks(); // On récupère tout l'objet tâche
    @GetMapping("/api/tasks/{id}")
    TaskDTO getTaskById(@PathVariable("id") Long id);
}
