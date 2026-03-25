package com.irrigo.weatherintelligenceservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskIntelligenceResponse {
    private Long taskId;
    private String taskName;
    private String crop;
    private String location;
    private double surface;
    private int duration;
    private double waterAmount;
    private LocalDateTime startTime;

    // Champs ajoutés pour la dimension professionnelle et multi-utilisateur
    private String userEmail;     // Propriétaire de la tâche
    private String soilProfile;   // Type de sol (SANDY, CLAYEY, etc.)
    private long plantAge;        // Âge de la plante en jours au moment de l'analyse

    private WeatherInfo weather;
    private String aiAdvice;
}