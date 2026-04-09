package com.irrigo.reportservice.dto;

import lombok.Data;

@Data
public class TaskDTO {
    private Long id;
    private String description;
    private String status;
    private Double consumption; // Supposons que tu as ce champ
    private String cropType;    // Type de culture (ex: pomme)
}