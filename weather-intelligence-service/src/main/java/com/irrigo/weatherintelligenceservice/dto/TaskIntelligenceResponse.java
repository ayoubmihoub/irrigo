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
    private int duration;
    private double waterAmount;
    private LocalDateTime startTime;
    private WeatherInfo weather;
    private String aiAdvice;
}