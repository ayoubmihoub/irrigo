package com.irrigo.weatherintelligenceservice.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data @AllArgsConstructor
public class IntelligenceResponse {
    private WeatherInfo weather;
    private List<CropAdvice> cropAdvices;

    @Data @AllArgsConstructor
    public static class CropAdvice {
        private String crop;
        private String advice;
    }
}
