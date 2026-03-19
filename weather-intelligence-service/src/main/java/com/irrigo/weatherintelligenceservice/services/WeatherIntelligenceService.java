package com.irrigo.weatherintelligenceservice.services;

import com.irrigo.taskmanagementservice.dto.TaskDTO;
import com.irrigo.weatherintelligenceservice.dto.*;
import com.irrigo.weatherintelligenceservice.clients.TaskServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class WeatherIntelligenceService {

    @Autowired
    private TaskServiceClient taskServiceClient;
    @Autowired
    private GeminiService geminiService;
    @Autowired
    private OpenWeatherService openWeatherService;

    public List<TaskIntelligenceResponse> getAutomatedInsights() {
        // 1. Récupérer l'intégralité des tâches
        List<TaskDTO> tasks = taskServiceClient.getAllTasks();
        if (tasks == null || tasks.isEmpty()) return Collections.emptyList();

        // 2. Météo de référence (utilisée pour le contexte global du conseil IA)
        WeatherInfo weather = openWeatherService.getWeather(tasks.get(0).getLocation());

        // 3. Appel groupé à Gemini (Batching) pour obtenir tous les conseils d'un coup
        Map<Long, String> bulkAdvices = geminiService.getBulkAIAdvice(tasks, weather);

        // 4. Construction de la liste des réponses enrichies
        return tasks.stream().map(task -> {
            String advice = bulkAdvices.getOrDefault(task.getId(), "advice indisponible pour le moment");

            return new TaskIntelligenceResponse(
                    task.getId(),
                    task.getName(),
                    task.getCrop(),
                    task.getLocation(),
                    task.getSurface(), // Champ ajouté précédemment
                    task.getDuration(),
                    task.getWaterAmount(),
                    task.getStartTime(),
                    weather,
                    advice
            );
        }).collect(Collectors.toList());
    }

    public TaskIntelligenceResponse getSingleTaskInsight(Long taskId) {
        TaskDTO task = taskServiceClient.getTaskById(taskId);
        WeatherInfo weather = openWeatherService.getWeather(task.getLocation());
        String advice = geminiService.getAIAdvice(task.getCrop(), weather);

        return new TaskIntelligenceResponse(
                task.getId(),
                task.getName(),
                task.getCrop(),
                task.getLocation(),
                task.getSurface(),
                task.getDuration(),
                task.getWaterAmount(),
                task.getStartTime(),
                weather,
                advice
        );
    }
}