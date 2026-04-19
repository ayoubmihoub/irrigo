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
    private ObjectMapper objectMapper;

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
     * Déclenche une irrigation immédiate (Statut: ongoing).
     */
    public void irrigateFarm(Long farmId, Double waterAmount, Integer duration, Double debit) {
        Farm farm = farmRepository.findById(farmId)
                .orElseThrow(() -> new RuntimeException("Champ non trouvé"));

        // Création du DTO pour le microservice Task
        TaskDTO taskDto = new TaskDTO();
        taskDto.setName("Irrigation : " + farm.getName());
        taskDto.setLocation(farm.getLocation());
        taskDto.setCrop(farm.getCrop());
        taskDto.setSurface(farm.getSurface());
        taskDto.setUserEmail(farm.getUserEmail());

        // Paramètres de l'action
        taskDto.setWaterAmount(waterAmount);
        taskDto.setDuration(duration);
        taskDto.setDebit(debit);

        // Logique temps réel : Heure actuelle et statut en cours
        taskDto.setStartTime(LocalDateTime.now());
        taskDto.setStatus("ongoing");

        // Transfert des données géospatiales
        try {
            if (farm.getParcelJson() != null && !farm.getParcelJson().isEmpty()) {
                ParcelDTO parcel = objectMapper.readValue(farm.getParcelJson(), ParcelDTO.class);
                taskDto.setParcel(parcel);
            }
        } catch (JsonProcessingException e) {
            System.err.println("Erreur GeoJSON lors de l'envoi vers TaskService");
        }

        // Appel Feign vers Task-Management-Service
        taskServiceClient.createIrrigationTask(taskDto);
    }
}