package com.irrigo.weatherintelligenceservice.services;

import com.irrigo.taskmanagementservice.dto.TaskDTO;
import com.irrigo.weatherintelligenceservice.dto.*;
import com.irrigo.weatherintelligenceservice.clients.TaskServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
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
        List<TaskDTO> tasks = taskServiceClient.getAllTasks();
        if (tasks == null || tasks.isEmpty()) return Collections.emptyList();

        // 1. Cache pour éviter de demander la météo plusieurs fois pour la même ville
        Map<String, WeatherInfo> weatherByCity = new HashMap<>();

        // 2. Maps pour lier les données spécifiques à chaque ID de tâche
        Map<Long, WeatherInfo> taskWeathers = new HashMap<>();
        Map<Long, Long> plantAges = new HashMap<>();
        Map<Long, Double> waterHistories = new HashMap<>();

        for (TaskDTO t : tasks) {
            // Récupération de la météo spécifique à la localisation de CHAQUE tâche
            WeatherInfo w = weatherByCity.computeIfAbsent(t.getLocation(),
                    loc -> openWeatherService.getWeather(loc));
            taskWeathers.put(t.getId(), w);

            // Calcul de l'âge de la plante
            plantAges.put(t.getId(), ChronoUnit.DAYS.between(t.getPlantingDate(), LocalDate.now()));

            // Récupération de l'historique d'arrosage (cumul 7 jours)
            Double history = taskServiceClient.getWaterHistory(t.getLocation(), t.getCrop(), 7);
            waterHistories.put(t.getId(), (history != null) ? history : 0.0);
        }

        // 3. Appel Bulk mis à jour (prend maintenant la Map des météos par tâche)
        Map<Long, String> bulkAdvices = geminiService.getBulkAIAdvice(tasks, taskWeathers, plantAges, waterHistories);

        return tasks.stream().map(task -> {
            String advice = bulkAdvices.getOrDefault(task.getId(), "Conseil indisponible");
            return new TaskIntelligenceResponse(
                    task.getId(), task.getName(), task.getCrop(), task.getLocation(),
                    task.getSurface(), task.getDuration(), task.getWaterAmount(),
                    task.getStartTime(), task.getUserEmail(), task.getSoilProfile(),
                    plantAges.get(task.getId()),
                    taskWeathers.get(task.getId()), // Injection de la météo spécifique à la tâche
                    advice
            );
        }).collect(Collectors.toList());
    }

    public TaskIntelligenceResponse getSingleTaskInsight(Long taskId) {
        TaskDTO task = taskServiceClient.getTaskById(taskId);
        WeatherInfo weather = openWeatherService.getWeather(task.getLocation());
        long plantAge = ChronoUnit.DAYS.between(task.getPlantingDate(), LocalDate.now());

        // Récupération de l'historique pour la tâche unique
        Double history = taskServiceClient.getWaterHistory(task.getLocation(), task.getCrop(), 7);
        double totalHistory = (history != null) ? history : 0.0;

        String advice = geminiService.generateExpertAdvice(
                task.getCrop(), task.getSoilProfile(), plantAge, weather, totalHistory
        );

        return new TaskIntelligenceResponse(
                task.getId(), task.getName(), task.getCrop(), task.getLocation(),
                task.getSurface(), task.getDuration(), task.getWaterAmount(),
                task.getStartTime(), task.getUserEmail(), task.getSoilProfile(),
                plantAge, weather, advice
        );
    }
}