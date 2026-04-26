package com.irrigo.weatherintelligenceservice.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "farm-task-service")
public interface FarmServiceClient {

    /**
     * Récupère l'objet Farm complet.
     * Utilisé pour extraire le 'name' et le 'parcelJson' (coordonnées GeoJSON).
     */
    @GetMapping("/api/farms/all")
    List<Object> getAllFarms();

    @GetMapping("/api/farms/{id}")
    Object getFarmById(@PathVariable("id") Long id);

    /**
     * Récupère uniquement la valeur de l'humidité.
     * Utilisé par l'ancienne méthode de conseil Gemini.
     */
    @GetMapping("/api/farms/{id}/moisture-value")
    Integer getMoisture(@PathVariable("id") Long id);
}