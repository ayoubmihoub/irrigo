package com.irrigo.farmservice.dto;

import lombok.Data;
import java.util.Map;

@Data
public class ParcelDTO {
    private String type; // "Feature"
    private GeometryDTO geometry;
    private Map<String, Object> properties;
}