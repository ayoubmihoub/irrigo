package com.irrigo.farmservice.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.irrigo.farmservice.dto.ParcelDTO;
import com.irrigo.farmservice.dto.TaskDTO;
import com.irrigo.farmservice.entities.Farm;
import com.irrigo.farmservice.repositories.FarmRepository;
import com.irrigo.farmservice.clients.TaskServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class FarmService {

    @Autowired
    private FarmRepository farmRepository;

    @Autowired
    private TaskServiceClient taskServiceClient;

    @Autowired
    private ObjectMapper objectMapper;

    // Scheduler pour gérer l'arrêt automatique de la LED
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private String getCurrentUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    public List<Farm> getMyFarms() {
        return farmRepository.findByUserEmail(getCurrentUserEmail());
    }

    public Farm saveFarm(Farm farm) {
        farm.setUserEmail(getCurrentUserEmail());
        return farmRepository.save(farm);
    }

    public void deleteFarm(Long id) {
        farmRepository.deleteById(id);
    }

    /**
     * Déclenche une irrigation immédiate.
     */
    public void irrigateFarm(Long farmId, Double waterAmount, Integer duration, Double debit) {
        Farm farm = farmRepository.findById(farmId)
                .orElseThrow(() -> new RuntimeException("Champ non trouvé"));

        // 1. Activer la LED pour l'ESP32
        farm.setIrrigationActive(true);
        farmRepository.saveAndFlush(farm);

        // 2. Préparation du DTO pour le microservice Task
        TaskDTO taskDto = new TaskDTO();
        taskDto.setName("Irrigation : " + farm.getName());
        taskDto.setLocation(farm.getLocation());
        taskDto.setCrop(farm.getCrop());
        taskDto.setSurface(farm.getSurface());
        taskDto.setUserEmail(farm.getUserEmail());
        taskDto.setWaterAmount(waterAmount);
        taskDto.setDuration(duration);
        taskDto.setDebit(debit);
        taskDto.setStartTime(LocalDateTime.now());
        taskDto.setStatus("ongoing");

        try {
            if (farm.getParcelJson() != null && !farm.getParcelJson().isEmpty()) {
                ParcelDTO parcel = objectMapper.readValue(farm.getParcelJson(), ParcelDTO.class);
                taskDto.setParcel(parcel);
            }
        } catch (JsonProcessingException e) {
            System.err.println("Erreur GeoJSON");
        }

        taskServiceClient.createIrrigationTask(taskDto);

        // 3. Planifier l'arrêt automatique (Timer)
        scheduler.schedule(() -> stopIrrigation(farmId), duration, TimeUnit.MINUTES);
    }

    public void stopIrrigation(Long id) {
        farmRepository.findById(id).ifPresent(f -> {
            f.setIrrigationActive(false);
            farmRepository.save(f);
        });
    }

    public Farm updateFarm(Long id, Farm details) {
        Farm farm = farmRepository.findById(id).orElseThrow();
        farm.setName(details.getName());
        farm.setCrop(details.getCrop());
        farm.setLocation(details.getLocation());
        farm.setSurface(details.getSurface());
        farm.setSoilProfile(details.getSoilProfile());
        return farmRepository.save(farm);
    }

    // --- LOGIQUE IOT : HUMIDITÉ ET STATUT ---

    public void updateFarmMoisture(Long id, Integer moisture) {
        farmRepository.findById(id).ifPresent(f -> {
            f.setCurrentMoisture(moisture);
            farmRepository.save(f);
        });
    }

    /**
     * RÉSOLUTION DE L'ERREUR : Récupère la valeur d'humidité pour l'IA.
     */
    public Integer getFarmMoisture(Long id) {
        return farmRepository.findById(id)
                .map(Farm::getCurrentMoisture)
                .orElse(0);
    }

    /**
     * Vérifie si l'irrigation est active pour l'ESP32.
     */
    public Boolean isIrrigationActive(Long id) {
        return farmRepository.findById(id)
                .map(Farm::getIrrigationActive)
                .orElse(false);
    }
    public Farm getFarmById(Long id) {
        return farmRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ferme non trouvée avec l'ID : " + id));
    }
}