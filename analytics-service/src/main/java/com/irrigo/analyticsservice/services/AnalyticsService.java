package com.irrigo.analyticsservice.services;

import com.irrigo.analyticsservice.clients.TaskServiceClient;
import com.irrigo.analyticsservice.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {
    @Autowired
    private TaskServiceClient taskServiceClient;

    public List<CropUsage> getWaterByCrop() {
        return taskServiceClient.getAllTasks().stream()
                .collect(Collectors.groupingBy(TaskDTO::getCrop, Collectors.summingDouble(TaskDTO::getWaterAmount)))
                .entrySet().stream().map(e -> new CropUsage(e.getKey(), e.getValue())).collect(Collectors.toList());
    }

    public List<DailyUsage> getWeeklyUsage() {
        return taskServiceClient.getAllTasks().stream()
                .collect(Collectors.groupingBy(t -> t.getStartTime().getDayOfWeek().name(), Collectors.summingDouble(TaskDTO::getWaterAmount)))
                .entrySet().stream().map(e -> new DailyUsage(e.getKey(), e.getValue())).collect(Collectors.toList());
    }

    public List<HourlyIntensity> getHourlyIntensity() {
        return taskServiceClient.getAllTasks().stream()
                .collect(Collectors.groupingBy(t -> t.getStartTime().getHour(), Collectors.summingDouble(TaskDTO::getWaterAmount)))
                .entrySet().stream().map(e -> new HourlyIntensity(e.getKey(), e.getValue()))
                .sorted(Comparator.comparing(HourlyIntensity::getHour)).collect(Collectors.toList());
    }

    public List<DailyDebit> getWeeklyDebit() {
        return taskServiceClient.getAllTasks().stream()
                .collect(Collectors.groupingBy(t -> t.getStartTime().getDayOfWeek().name(), Collectors.averagingDouble(TaskDTO::getDebit)))
                .entrySet().stream().map(e -> new DailyDebit(e.getKey(), e.getValue())).collect(Collectors.toList());
    }

    public List<DailySurface> getDailySurfaceUsage() {
        return taskServiceClient.getAllTasks().stream()
                .collect(Collectors.groupingBy(t -> t.getStartTime().toLocalDate().toString(), Collectors.summingDouble(TaskDTO::getSurface)))
                .entrySet().stream().map(e -> new DailySurface(e.getKey(), e.getValue()))
                .sorted(Comparator.comparing(DailySurface::getDate)).collect(Collectors.toList());
    }

    public ComparisonDTO getTodayVsYesterday() {
        List<TaskDTO> tasks = taskServiceClient.getAllTasks();
        LocalDate today = LocalDate.now();
        double todayVal = tasks.stream().filter(t -> t.getStartTime().toLocalDate().equals(today)).mapToDouble(TaskDTO::getWaterAmount).sum();
        double yesterdayVal = tasks.stream().filter(t -> t.getStartTime().toLocalDate().equals(today.minusDays(1))).mapToDouble(TaskDTO::getWaterAmount).sum();
        return new ComparisonDTO(todayVal, yesterdayVal);
    }
}