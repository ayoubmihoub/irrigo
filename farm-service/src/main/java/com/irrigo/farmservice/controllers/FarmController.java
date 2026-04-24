package com.irrigo.farmservice.controllers;

import com.irrigo.farmservice.entities.Farm;
import com.irrigo.farmservice.services.FarmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/farms")
public class FarmController {

    @Autowired
    private FarmService farmService;

    /**
     * Endpoint pour l'IA : GET /api/farms/{id}/moisture-value
     */
    @GetMapping("/{id}/moisture-value")
    public ResponseEntity<Integer> getMoistureValue(@PathVariable Long id) {
        return ResponseEntity.ok(farmService.getFarmMoisture(id));
    }

    /**
     * Endpoint pour l'ESP32 : PUT /api/farms/{id}/moisture
     */
    @PutMapping("/{id}/moisture")
    public ResponseEntity<Void> updateMoisture(@PathVariable Long id, @RequestBody Integer moisture) {
        farmService.updateFarmMoisture(id, moisture);
        return ResponseEntity.ok().build();
    }

    /**
     * Endpoint pour l'ESP32 : GET /api/farms/{id}/irrigation-status
     * MODIFICATION : On renvoie un String ("true" ou "false") pour correspondre
     * exactement à la lecture 'http.getString()' de l'Arduino.
     */
    @GetMapping("/{id}/irrigation-status")
    public String getStatus(@PathVariable Long id) {
        boolean active = farmService.isIrrigationActive(id);
        return String.valueOf(active); // Renvoie "true" ou "false" sans guillemets JSON
    }

    @GetMapping("/all")
    public List<Farm> getAll() {
        return farmService.getMyFarms();
    }

    @PostMapping("/create")
    public Farm create(@RequestBody Farm farm) {
        return farmService.saveFarm(farm);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Farm> update(@PathVariable Long id, @RequestBody Farm details) {
        return ResponseEntity.ok(farmService.updateFarm(id, details));
    }

    /**
     * Déclenche l'irrigation.
     * Comme ton FarmService fait déjà 'farm.setIrrigationActive(true)',
     * l'ESP32 verra le changement au prochain appel de getStatus().
     */
    @PostMapping("/{id}/irrigate")
    public ResponseEntity<String> irrigate(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        Double water = Double.valueOf(params.get("waterAmount").toString());
        Integer duration = Integer.valueOf(params.get("duration").toString());
        Double debit = Double.valueOf(params.get("debit").toString());

        farmService.irrigateFarm(id, water, duration, debit);
        return ResponseEntity.ok("Irrigation lancée ! L'ESP32 va s'activer d'ici quelques secondes.");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        farmService.deleteFarm(id);
        return ResponseEntity.ok("Champ supprimé avec succès !");
    }
}