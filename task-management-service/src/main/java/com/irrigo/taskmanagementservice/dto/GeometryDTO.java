package com.irrigo.taskmanagementservice.dto;
import lombok.Data;
import java.util.List;

@Data
public class GeometryDTO {
    private String type; // ex: "Polygon"
    private List<List<List<Double>>> coordinates;
}