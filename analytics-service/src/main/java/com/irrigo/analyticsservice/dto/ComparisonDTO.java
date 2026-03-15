package com.irrigo.analyticsservice.dto;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ComparisonDTO {
    private double today;
    private double yesterday;
}