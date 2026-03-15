package com.irrigo.analyticsservice.dto;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DailySurface {
    private String date;
    private double totalSurface;
}
