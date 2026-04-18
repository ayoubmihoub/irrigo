package com.irrigo.farmservice.dto;

import lombok.Data;
import java.util.Map;

@Data
public class SubParcelDTO {
    private String type;
    private GeometryDTO geometry;
    private Map<String, String> properties; // Pour le nom de la zone
}