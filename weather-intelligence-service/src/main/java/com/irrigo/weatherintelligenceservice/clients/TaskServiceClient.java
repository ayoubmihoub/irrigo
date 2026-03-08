package com.irrigo.weatherintelligenceservice.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

@FeignClient(name = "task-management-service")
public interface TaskServiceClient {
    @GetMapping("/api/tasks/crops/unique")
    List<String> getUniqueCrops();
}
