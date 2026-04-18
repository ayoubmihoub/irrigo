package com.irrigo.weatherintelligenceservice.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "task-management-service")
public interface TaskServiceClient {

    @GetMapping("/api/tasks/history/sum")
    Double getWaterHistory(
            @RequestParam("location") String location,
            @RequestParam("crop") String crop,
            @RequestParam("days") int days
    );
}