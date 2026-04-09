package com.irrigo.reportservice.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class IrrigationTaskDTO {
    private Long id;
    private String name;
    private String status;
    private String crop;
    private Double waterAmount;
    private Double debit;
    private Double surface;
    private LocalDateTime startTime; // Ajout du champ date/heure
}