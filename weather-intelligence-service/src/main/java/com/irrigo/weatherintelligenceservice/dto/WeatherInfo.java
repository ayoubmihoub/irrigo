package com.irrigo.weatherintelligenceservice.dto;


import lombok.Data;

@Data
public class WeatherInfo {
    private String cityName;
    private double temperature;
    private int humidity;
    private double rainProbability; // Récupéré via le champ 'pop'
    private String description;
}
