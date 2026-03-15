package com.irrigo.analyticsservice.dto;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HourlyIntensity {
    private int hour;
    private double totalWater;
}
