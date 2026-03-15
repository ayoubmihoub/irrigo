package com.irrigo.analyticsservice.controllers;

import com.irrigo.analyticsservice.dto.*;
import com.irrigo.analyticsservice.services.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {
    @Autowired
    private AnalyticsService service;

    @GetMapping("/crop") public List<CropUsage> getCropUsage() { return service.getWaterByCrop(); }
    @GetMapping("/weekly") public List<DailyUsage> getWeekly() { return service.getWeeklyUsage(); }
    @GetMapping("/hourly") public List<HourlyIntensity> getHourly() { return service.getHourlyIntensity(); }
    @GetMapping("/debit") public List<DailyDebit> getDebit() { return service.getWeeklyDebit(); }
    @GetMapping("/surface") public List<DailySurface> getSurface() { return service.getDailySurfaceUsage(); }
    @GetMapping("/compare") public ComparisonDTO getCompare() { return service.getTodayVsYesterday(); }
}