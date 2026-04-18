package com.irrigo.weatherintelligenceservice.controllers;

import com.irrigo.weatherintelligenceservice.dto.TaskAdviceRequest;
import com.irrigo.weatherintelligenceservice.services.WeatherIntelligenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/weather")
public class WeatherController {

    @Autowired
    private WeatherIntelligenceService weatherIntelligenceService;

    /**
     * Endpoint pour demander l'avis de l'IA avant de créer la tâche.
     * Reçoit les 4 paramètres via TaskAdviceRequest.
     */
    @PostMapping("/ask-advice")
    public ResponseEntity<Map<String, String>> askAdvice(@RequestBody TaskAdviceRequest request) {
        String advice = weatherIntelligenceService.getPreTaskAdvice(request);
        return ResponseEntity.ok(Map.of("advice", advice));
    }
}