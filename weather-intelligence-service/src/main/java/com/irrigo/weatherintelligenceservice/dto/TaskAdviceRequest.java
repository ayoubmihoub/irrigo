package com.irrigo.weatherintelligenceservice.dto;

import lombok.Data;

@Data
public class TaskAdviceRequest {
    private Long farmId; // Ajoutez cet ID
    private String crop;
    private String soilProfile;
}