package com.irrigo.weatherintelligenceservice.services;

import com.irrigo.weatherintelligenceservice.dto.TaskAdviceRequest;
import com.irrigo.weatherintelligenceservice.clients.FarmServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WeatherIntelligenceService {

    @Autowired
    private GeminiService geminiService;

    @Autowired
    private FarmServiceClient farmServiceClient;

    public String getPreTaskAdvice(TaskAdviceRequest request) {
        // 1. RÉCUPÉRATION DE L'HUMIDITÉ RÉELLE (ESP32)
        Integer realMoisture = 0;
        try {
            System.out.println("DEBUG: Appel Feign pour la ferme ID: " + request.getFarmId());
            // Utilise getMoisture si c'est le nom dans ton interface Feign
            realMoisture = farmServiceClient.getMoisture(request.getFarmId());

            if (realMoisture == null) {
                System.err.println("WARN: FarmService a renvoyé NULL, utilisation de 0%");
                realMoisture = 0;
            }
            System.out.println("DEBUG: Humidité récupérée: " + realMoisture + "%");

        } catch (Exception e) {
            System.err.println("ERREUR FEIGN: Impossible de contacter FarmService: " + e.getMessage());
            realMoisture = 0; // Sécurité
        }

        // 2. APPEL À GEMINI (Avec gestion d'erreur dédiée)
        try {
            System.out.println("DEBUG: Envoi de la requête à Gemini (Culture: " + request.getCrop() + ")");

            String aiResponse = geminiService.generateDecisionAdvice(
                    request.getCrop(),
                    request.getSoilProfile(),
                    realMoisture
            );

            if (aiResponse == null || aiResponse.trim().isEmpty()) {
                throw new RuntimeException("Gemini a renvoyé une réponse vide ou nulle.");
            }

            return aiResponse;

        } catch (Exception e) {
            // C'est ici que tu verras l'erreur réelle (Clé API, Quota, etc.)
            System.err.println("ERREUR CRITIQUE GEMINI: " + e.getMessage());
            e.printStackTrace();

            return "Conseil indisponible (Erreur IA). Note technique : " + e.getMessage()
                    + ". Basez-vous sur l'humidité actuelle de " + realMoisture + "%.";
        }
    }
}