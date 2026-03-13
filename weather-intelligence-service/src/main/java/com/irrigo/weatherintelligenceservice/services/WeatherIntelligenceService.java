package com.irrigo.weatherintelligenceservice.services;

import com.irrigo.taskmanagementservice.dto.TaskDTO;
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

    public List<TaskIntelligenceResponse> getAutomatedInsights() {
        // 1. Récupérer toutes les tâches réelles
        List<TaskDTO> tasks = taskServiceClient.getAllTasks();
        List<TaskIntelligenceResponse> responses = new ArrayList<>();

        // Dans la méthode getAutomatedInsights() de WeatherIntelligenceService.java

        for (TaskDTO task : tasks) {
            try {
                // 1. Récupération de la météo basée sur la localisation de la tâche
                WeatherInfo weather = openWeatherService.getWeather(task.getLocation());

                // 2. Génération du conseil IA
                String advice = geminiService.getAIAdvice(task.getCrop(), weather);

                // 3. Création de la réponse enrichie avec TOUS les champs
                responses.add(new TaskIntelligenceResponse(
                        task.getId(),           // taskId
                        task.getName(),         // taskName
                        task.getCrop(),         // crop
                        task.getLocation(),     // location
                        task.getDuration(),     // duration (Nouveau)
                        task.getWaterAmount(),  // waterAmount (Nouveau)
                        task.getStartTime(),    // startTime (Nouveau)
                        weather,                // weather object
                        advice                  // aiAdvice
                ));

            } catch (Exception e) {
                System.err.println("Erreur de traitement pour la tâche " + task.getName() + " : " + e.getMessage());
            }
        }
        return responses;
    }
}