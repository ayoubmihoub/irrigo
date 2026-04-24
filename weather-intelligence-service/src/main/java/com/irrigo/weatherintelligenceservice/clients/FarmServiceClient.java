package com.irrigo.weatherintelligenceservice.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "farm-task-service")
public interface FarmServiceClient {

    // Le nom ici doit être EXACTEMENT celui que tu appelles dans ton service
    @GetMapping("/api/farms/{id}/moisture-value")
    Integer getMoisture(@PathVariable("id") Long id);
}