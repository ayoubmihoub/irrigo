package com.irrigo.analyticsservice.dto;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data @AllArgsConstructor
public class CropUsage { private String crop; private double totalWater; }