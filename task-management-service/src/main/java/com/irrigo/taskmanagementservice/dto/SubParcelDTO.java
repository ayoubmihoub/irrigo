package com.irrigo.taskmanagementservice.dto;
import lombok.Data;
import java.util.Map;

@Data
public class SubParcelDTO {
    private String type;
    private GeometryDTO geometry;
    private Map<String, String> properties; // Pour zoneName
}