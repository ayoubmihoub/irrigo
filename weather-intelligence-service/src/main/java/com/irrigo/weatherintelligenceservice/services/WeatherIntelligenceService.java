package com.irrigo.weatherintelligenceservice.services;

import com.irrigo.weatherintelligenceservice.dto.*;
import com.irrigo.weatherintelligenceservice.clients.TaskServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class WeatherIntelligenceService {

    @Autowired
    private TaskServiceClient taskServiceClient;

    @Autowired
    private GeminiService geminiService;

    @Autowired
    private OpenWeatherService openWeatherService;

    public IntelligenceResponse getFullInsights(String city) {
        // 1. Récupérer la météo réelle via OpenWeatherMap
        WeatherInfo weather = openWeatherService.getWeather(city);

        // 2. Récupérer la liste des cultures uniques depuis le service de tâches
        List<String> crops = taskServiceClient.getUniqueCrops();

        List<IntelligenceResponse.CropAdvice> advices = new ArrayList<>();

        // 3. Demander à Gemini un conseil personnalisé pour chaque culture
        for (String crop : crops) {
            String adviceText = geminiService.getAIAdvice(crop, weather);
            advices.add(new IntelligenceResponse.CropAdvice(crop, adviceText));
        }

        return new IntelligenceResponse(weather, advices);
    }
}