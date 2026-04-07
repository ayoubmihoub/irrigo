package com.irrigo.taskmanagementservice.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tasks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamicInsert // Permet d'ignorer les champs nulls pour utiliser les DEFAULT de la base de données
public class IrrigationTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String location;

    // --- CHANGEMENT : Utilisation de Double/Integer (Wrappers) pour accepter le null ---
    private Double surface;
    private Integer duration;
    private Double waterAmount;
    private Double debit;

    private LocalDateTime startTime;
    private String crop;
    private String userEmail;

    @Enumerated(EnumType.STRING)
    private ETaskStatus status;

    @Enumerated(EnumType.STRING)
    private ESoilType soilProfile;

    private LocalDate plantingDate;


}