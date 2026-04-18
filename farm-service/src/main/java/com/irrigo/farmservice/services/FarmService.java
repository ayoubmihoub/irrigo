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

@Service
public class FarmService {

    @Autowired
    private FarmRepository farmRepository;

    @Autowired
    private TaskServiceClient taskServiceClient;

    @Autowired
    private ObjectMapper objectMapper; // Indispensable pour traiter le GeoJSON

    /**
     * Récupère l'email de l'utilisateur connecté via le SecurityContext.
     */
    private String getCurrentUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    // --- CRUD Classique ---

    public List<Farm> getMyFarms() {
        return farmRepository.findByUserEmail(getCurrentUserEmail());
    }

    public Farm saveFarm(Farm farm) {
        farm.setUserEmail(getCurrentUserEmail());
        return farmRepository.save(farm);
    }

    public Farm updateFarm(Long id, Farm details) {
        Farm farm = farmRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Champ non trouvé"));

        farm.setName(details.getName());
        farm.setCrop(details.getCrop());
        farm.setLocation(details.getLocation());
        farm.setSurface(details.getSurface());
        farm.setSoilProfile(details.getSoilProfile());
        farm.setImage(details.getImage());
        farm.setParcelJson(details.getParcelJson());

        return farmRepository.save(farm);
    }

    public void deleteFarm(Long id) {
        farmRepository.deleteById(id);
    }

    /**
     * Méthode d'irrigation : Transforme un champ en une tâche planifiée dans le Task Service.
     * Récupère les données géospatiales (Polygone) pour une précision maximale.
     */
    public void irrigateFarm(Long farmId, Double waterAmount, Integer duration, Double debit) {
        Farm farm = farmRepository.findById(farmId)
                .orElseThrow(() -> new RuntimeException("Champ non trouvé avec l'ID : " + farmId));

        // 1. Construction du DTO pour le Task-Management-Service
        TaskDTO taskDto = new TaskDTO();
        taskDto.setName("Irrigation automatique : " + farm.getName());
        taskDto.setLocation(farm.getLocation());
        taskDto.setCrop(farm.getCrop());
        taskDto.setSurface(farm.getSurface());
        taskDto.setSoilProfile(farm.getSoilProfile() != null ? farm.getSoilProfile().name() : null);
        taskDto.setUserEmail(farm.getUserEmail());

        // 2. Intégration des paramètres de session d'arrosage
        taskDto.setWaterAmount(waterAmount);
        taskDto.setDuration(duration);
        taskDto.setDebit(debit);
        taskDto.setStartTime(LocalDateTime.now().plusMinutes(5)); // Planifié dans 5 min par défaut
        taskDto.setStatus("planned");

        // 3. Désérialisation du GeoJSON pour l'envoyer au Task Service
        try {
            if (farm.getParcelJson() != null && !farm.getParcelJson().isEmpty()) {
                ParcelDTO parcel = objectMapper.readValue(farm.getParcelJson(), ParcelDTO.class);
                taskDto.setParcel(parcel);
            }
        } catch (JsonProcessingException e) {
            // En cas d'erreur, on envoie une tâche sans polygone pour ne pas bloquer l'irrigation
            System.err.println("Erreur lors de la lecture des données GeoJSON de la parcelle : " + e.getMessage());
        }

        // 4. Appel au microservice Task via Feign
        taskServiceClient.createIrrigationTask(taskDto);
    }
}