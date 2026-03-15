package com.irrigo.analyticsservice.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TaskDTO {
    private Long id;
    private String name;
    private String crop;
    private double waterAmount;
    private double debit;
    private double surface;
    private LocalDateTime startTime;
}