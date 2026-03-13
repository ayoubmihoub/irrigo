package com.irrigo.weatherintelligenceservice.controllers;

import com.irrigo.weatherintelligenceservice.dto.IntelligenceResponse;
import com.irrigo.weatherintelligenceservice.dto.TaskIntelligenceResponse;
import com.irrigo.weatherintelligenceservice.services.WeatherIntelligenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/weather")
public class WeatherController {

    @Autowired
    private WeatherIntelligenceService intelligenceService;

    @GetMapping("/automated-insights")
    public ResponseEntity<List<TaskIntelligenceResponse>> getAutomatedInsights() {
        return ResponseEntity.ok(intelligenceService.getAutomatedInsights());
    }
}