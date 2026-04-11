package com.irrigo.botservice.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Service
public class GeminiService {

    @Value("${api.gemini.key}")
    private String apiKey;

    // URL corrigée selon votre guide cURL : retrait du "1.5"
    private final String apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent";
    private final RestTemplate restTemplate = new RestTemplate();

    public String chatWithBot(String userMessage) {
        String prompt = "Tu es un expert agronome pour la plateforme Irrigo. Réponds de manière concise : " + userMessage;
        return callGemini(List.of(Map.of("text", prompt)));
    }

    public String analyzePlantDisease(byte[] imageBytes) {
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);

        Map<String, Object> textPart = Map.of("text", "Analyse cette photo de plante. Identifie la maladie et donne des conseils de traitement.");
        Map<String, Object> imagePart = Map.of("inlineData", Map.of("mimeType", "image/jpeg", "data", base64Image));

        return callGemini(List.of(textPart, imagePart));
    }

    private String callGemini(List<Map<String, Object>> parts) {
        // Structure du corps identique au guide cURL
        Map<String, Object> contentItem = Map.of("parts", parts);
        Map<String, Object> requestBody = Map.of("contents", List.of(contentItem));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-goog-api-key", apiKey);

        try {
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, entity, Map.class);

            // Logique d'extraction validée par votre service météo
            List candidates = (List) response.getBody().get("candidates");
            if (candidates != null && !candidates.isEmpty()) {
                Map candidate = (Map) candidates.get(0);
                Map content = (Map) candidate.get("content");
                List resParts = (List) content.get("parts");
                Map part = (Map) resParts.get(0);
                return (String) part.get("text");
            }
            return "L'IA n'a pas renvoyé de réponse.";
        } catch (Exception e) {
            return "Erreur Gemini : " + e.getMessage();
        }
    }
}