package com.irrigo.usermanagementservice.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;
import java.util.Map;

@FeignClient(name = "task-management-service")
public interface TaskServiceClient {
    @GetMapping("/api/tasks/all")
    List<Map<String, Object>> getAllTasksFromService();
}