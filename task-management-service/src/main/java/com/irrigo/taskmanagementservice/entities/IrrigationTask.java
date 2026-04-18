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
@DynamicInsert
public class IrrigationTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String location;
    private Double surface;
    private Integer duration;
    private Double waterAmount;
    private Double debit;
    private LocalDateTime startTime;
    private String crop;
    private String userEmail;

    @Column(length = 1000)
    private String aiAdvice; // Stocke le conseil ou la raison d'annulation

    @Enumerated(EnumType.STRING)
    private ETaskStatus status;

    @Enumerated(EnumType.STRING)
    private ESoilType soilProfile;

    private LocalDate plantingDate;

    // Stockage JSON pour éviter la complexité Hibernate Spatial
    @Column(columnDefinition = "TEXT")
    private String parcelJson;

    @Column(columnDefinition = "TEXT")
    private String subParcelsJson;
}