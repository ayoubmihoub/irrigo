package com.irrigo.taskmanagementservice.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tasks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IrrigationTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String location;
    private double surface;
    private int duration;
    private double waterAmount;
    private double debit;
    private LocalDateTime startTime;
    private String crop;
    private String userEmail; // AJOUT : Identifiant du propriétaire

    @Enumerated(EnumType.STRING)
    private ETaskStatus status;

    @Enumerated(EnumType.STRING)
    private ESoilType soilProfile;

    private LocalDate plantingDate;
}