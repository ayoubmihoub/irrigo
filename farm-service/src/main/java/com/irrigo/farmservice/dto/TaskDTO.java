package com.irrigo.farmservice.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class TaskDTO {
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
    private String status;
    private String soilProfile;
    private LocalDate plantingDate;
    private String aiAdvice;

    // Données géospatiales
    private ParcelDTO parcel;
    private List<SubParcelDTO> subParcels;
}