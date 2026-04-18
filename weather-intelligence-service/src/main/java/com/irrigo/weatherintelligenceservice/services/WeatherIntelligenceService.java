package com.irrigo.weatherintelligenceservice.services;

import com.irrigo.weatherintelligenceservice.dto.TaskAdviceRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WeatherIntelligenceService {

    @Autowired
    private GeminiService geminiService;

    public String getPreTaskAdvice(TaskAdviceRequest request) {
        // 1. Fake Current Moisture (Simulation ESP32)
        int fakeMoisture = 35; // 35% d'humidité du sol

        // 2. Appel à Gemini avec tes 3 paramètres : Crop, Soil, Moisture
        return geminiService.generateDecisionAdvice(
                request.getCrop(),
                request.getSoilProfile(),
                fakeMoisture
        );
    }
}