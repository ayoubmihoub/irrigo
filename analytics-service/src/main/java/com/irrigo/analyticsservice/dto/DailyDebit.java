package com.irrigo.analyticsservice.dto;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DailyDebit {
    private String day;
    private double averageDebit;
}
