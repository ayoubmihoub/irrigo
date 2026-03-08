package com.irrigo.weatherintelligenceservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = "com.irrigo.weatherintelligenceservice") // Force le scan ici
@EnableFeignClients
@EnableDiscoveryClient
public class WeatherIntelligenceServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(WeatherIntelligenceServiceApplication.class, args);
    }
}
