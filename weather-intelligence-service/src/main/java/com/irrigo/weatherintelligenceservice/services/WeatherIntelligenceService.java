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
        // 1. Récupérer TOUTES les tâches (pas seulement les cultures uniques)
        List<TaskDTO> tasks = taskServiceClient.getAllTasks();
        List<TaskIntelligenceResponse> responses = new ArrayList<>();

        for (TaskDTO task : tasks) {
            try {
                // 2. Météo pour chaque emplacement de tâche
                WeatherInfo weather = openWeatherService.getWeather(task.getLocation());

                // 3. Conseil IA
                String advice = geminiService.getAIAdvice(task.getCrop(), weather);

                // 4. Ajouter à la liste finale
                responses.add(new TaskIntelligenceResponse(
                        task.getId(),
                        task.getName(),
                        task.getCrop(),
                        task.getLocation(),
                        task.getDuration(),
                        task.getWaterAmount(),
                        task.getStartTime(),
                        weather,
                        advice
                ));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return responses;
    }
    public TaskIntelligenceResponse getSingleTaskInsight(Long taskId) {
        // 1. Récupérer la tâche spécifique via Feign
        TaskDTO task = taskServiceClient.getTaskById(taskId);

        // 2. Récupérer la météo et le conseil (comme dans la boucle)
        WeatherInfo weather = openWeatherService.getWeather(task.getLocation());
        String advice = geminiService.getAIAdvice(task.getCrop(), weather);

        // 3. Retourner l'objet complet
        return new TaskIntelligenceResponse(
                task.getId(),
                task.getName(),
                task.getCrop(),
                task.getLocation(),
                task.getDuration(),
                task.getWaterAmount(),
                task.getStartTime(),
                weather,
                advice
        );
    }
}