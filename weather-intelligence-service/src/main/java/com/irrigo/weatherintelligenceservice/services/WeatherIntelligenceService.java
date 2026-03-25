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

    /**
     * Analyse groupée pour le tableau de bord global.
     */
    public List<TaskIntelligenceResponse> getAutomatedInsights() {
        List<TaskDTO> tasks = taskServiceClient.getAllTasks();
        if (tasks == null || tasks.isEmpty()) return Collections.emptyList();

        // On récupère la météo (ici simplifiée sur la première localisation pour le batch)
        WeatherInfo weather = openWeatherService.getWeather(tasks.get(0).getLocation());

        // Calcul des âges pour chaque tâche avant l'envoi à l'IA
        Map<Long, Long> plantAges = tasks.stream()
                .collect(Collectors.toMap(
                        TaskDTO::getId,
                        t -> ChronoUnit.DAYS.between(t.getPlantingDate(), LocalDate.now())
                ));

        // Appel à Gemini avec le nouveau contexte agronomique
        Map<Long, String> bulkAdvices = geminiService.getBulkAIAdvice(tasks, weather, plantAges);

        return tasks.stream().map(task -> {
            String advice = bulkAdvices.getOrDefault(task.getId(), "Conseil indisponible");

            return new TaskIntelligenceResponse(
                    task.getId(),
                    task.getName(),
                    task.getCrop(),
                    task.getLocation(),
                    task.getSurface(),
                    task.getDuration(),
                    task.getWaterAmount(),
                    task.getStartTime(),
                    task.getUserEmail(),    // Propriétaire
                    task.getSoilProfile(), // Profil du sol
                    plantAges.get(task.getId()), // Âge calculé
                    weather,
                    advice
            );
        }).collect(Collectors.toList());
    }

    /**
     * Analyse détaillée pour une tâche spécifique.
     */
    public TaskIntelligenceResponse getSingleTaskInsight(Long taskId) {
        // 1. Récupération des données complètes (incluant plantingDate, soilProfile, userEmail)
        TaskDTO task = taskServiceClient.getTaskById(taskId);

        // 2. Récupération de la météo précise (Temp, Humidité, Probabilité de pluie)
        WeatherInfo weather = openWeatherService.getWeather(task.getLocation());

        // 3. Calcul de l'âge de la plante
        long plantAge = ChronoUnit.DAYS.between(task.getPlantingDate(), LocalDate.now());

        // 4. Génération du conseil professionnel via l'IA
        // On passe désormais : Culture, Sol, Âge et l'objet Weather complet
        String advice = geminiService.generateExpertAdvice(
                task.getCrop(),
                task.getSoilProfile(),
                plantAge,
                weather
        );

        return new TaskIntelligenceResponse(
                task.getId(),
                task.getName(),
                task.getCrop(),
                task.getLocation(),
                task.getSurface(),
                task.getDuration(),
                task.getWaterAmount(),
                task.getStartTime(),
                task.getUserEmail(),
                task.getSoilProfile(),
                plantAge,
                weather,
                advice
        );
    }
}