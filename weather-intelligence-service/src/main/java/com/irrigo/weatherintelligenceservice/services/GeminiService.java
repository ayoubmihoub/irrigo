package com.irrigo.weatherintelligenceservice.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Service
public class GeminiService {

    @Value("${api.gemini.key}")
    private String apiKey;

    private final String apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent";
    private final RestTemplate restTemplate = new RestTemplate();

    public String generateDecisionAdvice(String crop, String soil, int currentMoisture) {
        // On inclut currentMoisture dans le formatage final pour la justification
        String prompt = String.format(
                "Tu es un agronome expert en IoT. Analyse ces données en temps réel :\n" +
                        "- Culture : %s\n" +
                        "- Type de sol : %s\n" +
                        "- Humidité actuelle du sol (Capteur ESP32) : %d%%\n\n" +
                        "Consigne :\n" +
                        "1. Réponds par 'OUI, vous pouvez irriguer' ou 'NON, attendez encore'.\n" +
                        "2. Justifie brièvement (2 phrases max) en expliquant si le taux d'humidité de %d%% " +
                        "est suffisant ou critique pour une culture de type %s dans un sol %s.",
                crop, soil, currentMoisture, currentMoisture, crop, soil
        );

        // ✅ On passe maintenant l'humidité à la méthode interne
        return callGeminiInternal(prompt, currentMoisture);
    }

    // ✅ La méthode accepte maintenant 'currentMoisture' pour pouvoir l'utiliser dans le catch
    private String callGeminiInternal(String promptText, int currentMoisture) {
        Map<String, Object> textPart = Map.of("text", promptText);
        Map<String, Object> parts = Map.of("parts", List.of(textPart));
        Map<String, Object> content = Map.of("contents", List.of(parts));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-goog-api-key", apiKey);

        try {
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(content, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, entity, Map.class);

            List candidates = (List) response.getBody().get("candidates");
            Map candidate = (Map) candidates.get(0);
            Map contentRes = (Map) candidate.get("content");
            List partsRes = (List) contentRes.get("parts");

            return (String) ((Map) partsRes.get(0)).get("text");
        } catch (Exception e) {
            // ✅ Maintenant cette ligne fonctionne car currentMoisture est dans la portée (scope)
            return "Conseil indisponible. Basez-vous sur l'humidité actuelle de " + currentMoisture + "%.";
        }
    }
}