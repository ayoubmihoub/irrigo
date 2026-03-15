package com.irrigo.analyticsservice.clients;

import com.irrigo.analyticsservice.dto.TaskDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

@FeignClient(name = "task-management-service")
public interface TaskServiceClient {
    @GetMapping("/api/tasks/all")
    List<TaskDTO> getAllTasks();
}