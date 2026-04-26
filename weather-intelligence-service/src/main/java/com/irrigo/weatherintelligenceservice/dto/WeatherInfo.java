package com.irrigo.weatherintelligenceservice.dto;

import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeatherInfo {
    private String cityName;
    private CurrentWeather current;
    private List<ForecastDay> forecast;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CurrentWeather {
        private double temperature;
        private int humidity;
        private String description;
        private int rainProbability; // Pourcentage entier
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ForecastDay {
        private String date;
        private double tempMin;
        private double tempMax;
        private String description;
        private int rainProbability; // Ajouté et passé en pourcentage entier
    }
}