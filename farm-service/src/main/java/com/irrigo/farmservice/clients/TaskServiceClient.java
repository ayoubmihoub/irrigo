package com.irrigo.farmservice.clients;

import com.irrigo.farmservice.dto.TaskDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "task-management-service")
public interface TaskServiceClient {
    @PostMapping("/api/tasks/create")
    void createIrrigationTask(@RequestBody TaskDTO taskDto);
}