package com.irrigo.weatherintelligenceservice.controllers;

import com.irrigo.weatherintelligenceservice.dto.IntelligenceResponse;
import com.irrigo.weatherintelligenceservice.services.WeatherIntelligenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/weather")
public class WeatherController {

    @Autowired
    private WeatherIntelligenceService intelligenceService;

    @GetMapping("/insights")
    public ResponseEntity<IntelligenceResponse> getWeatherInsights(@RequestParam String city) {
        return ResponseEntity.ok(intelligenceService.getFullInsights(city));
    }
}