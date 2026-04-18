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
     * Récupère tous les champs de l'utilisateur connecté.
     */
    @GetMapping
    public List<Farm> getAll() {
        return farmService.getMyFarms();
    }

    /**
     * Crée un nouveau champ.
     */
    @PostMapping("/create")
    public Farm create(@RequestBody Farm farm) {
        return farmService.saveFarm(farm);
    }

    /**
     * Met à jour les informations d'un champ existant.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Farm> update(@PathVariable Long id, @RequestBody Farm details) {
        return ResponseEntity.ok(farmService.updateFarm(id, details));
    }

    /**
     * Endpoint pour lancer une irrigation sur un champ existant.
     * Les données du champ sont fusionnées avec les paramètres d'arrosage saisis.
     */
    @PostMapping("/{id}/irrigate")
    public ResponseEntity<String> irrigate(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        // Extraction sécurisée des paramètres depuis la Map
        Double water = Double.valueOf(params.get("waterAmount").toString());
        Integer duration = Integer.valueOf(params.get("duration").toString());
        Double debit = Double.valueOf(params.get("debit").toString());

        farmService.irrigateFarm(id, water, duration, debit);
        return ResponseEntity.ok("Demande d'irrigation transmise au Task Service !");
    }

    /**
     * Supprime un champ.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        farmService.deleteFarm(id);
        return ResponseEntity.ok("Champ supprimé avec succès !");
    }
}