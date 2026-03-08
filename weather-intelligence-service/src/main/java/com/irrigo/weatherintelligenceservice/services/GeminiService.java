package com.irrigo.weatherintelligenceservice.services;

import com.irrigo.weatherintelligenceservice.dto.WeatherInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Service
public class GeminiService {

    @Value("${api.gemini.key}")
    private String apiKey;

    // URL mise à jour selon ton cURL
    private final String apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent";
    private final RestTemplate restTemplate = new RestTemplate();

    public String getAIAdvice(String crop, WeatherInfo weather) {
        String description = (weather.getDescription() != null) ? weather.getDescription() : "Ensoleillé";

        String promptText = String.format(
                "En tant qu'expert agronome, donne un conseil d'irrigation très court (max 15 mots) pour la culture de %s. " +
                        "Météo: %s, Temp: %.1f°C, Humidité: %d%%.",
                crop, description, weather.getTemperature(), weather.getHumidity()
        );

        // Construction du corps de la requête (Structure identique au cURL)
        Map<String, Object> textPart = Map.of("text", promptText);
        Map<String, Object> parts = Map.of("parts", List.of(textPart));
        Map<String, Object> contentItem = Map.of("parts", List.of(textPart));
        Map<String, Object> requestBody = Map.of("contents", List.of(contentItem));

        // Configuration des Headers (X-goog-api-key est requis ici)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-goog-api-key", apiKey);

        try {
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // Appel POST sans la clé dans l'URL (car elle est dans le header)
            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, entity, Map.class);

            // Extraction de la réponse
            List candidates = (List) response.getBody().get("candidates");
            if (candidates != null && !candidates.isEmpty()) {
                Map candidate = (Map) candidates.get(0);
                Map content = (Map) candidate.get("content");
                List resParts = (List) content.get("parts");
                Map part = (Map) resParts.get(0);
                return (String) part.get("text");
            }
            return "L'IA n'a pas renvoyé de contenu.";

        } catch (Exception e) {
            e.printStackTrace();
            return "Erreur Gemini : " + e.getMessage();
        }
    }
}