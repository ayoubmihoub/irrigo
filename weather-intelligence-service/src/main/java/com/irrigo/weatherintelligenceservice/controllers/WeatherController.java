package com.irrigo.weatherintelligenceservice.controllers;

import com.irrigo.weatherintelligenceservice.dto.TaskAdviceRequest;
import com.irrigo.weatherintelligenceservice.services.WeatherIntelligenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/weather")
public class WeatherController {

    @Autowired
    private WeatherIntelligenceService weatherService; // Nommé 'weatherService' pour correspondre aux méthodes

    /**
     * Retourne la météo de TOUTES les fermes de l'utilisateur connecté.
     */
    @GetMapping("/all-status")
    public ResponseEntity<List<Map<String, Object>>> getAllStatus() {
        return ResponseEntity.ok(weatherService.getAllFarmsWeatherStatus());
    }

    /**
     * Retourne la météo d'une ferme spécifique via son ID.
     */
    @GetMapping("/farm-status/{id}")
    public ResponseEntity<Map<String, Object>> getFarmStatus(@PathVariable Long id) {
        return ResponseEntity.ok(weatherService.getFarmWeatherStatus(id));
    }

    /**
     * Endpoint pour demander l'avis de l'IA Gemini.
     */
    @PostMapping("/ask-advice")
    public ResponseEntity<Map<String, String>> askAdvice(@RequestBody TaskAdviceRequest request) {
        String advice = weatherService.getPreTaskAdvice(request);
        return ResponseEntity.ok(Map.of("advice", advice));
    }
}