package com.irrigo.taskmanagementservice.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TaskDTO {
    private Long id;
    private String name;
    private String location;
    private double surface;
    private int duration;
    private double waterAmount;
    private double debit;
    private LocalDateTime startTime;
    private String crop;
    private String status;
}