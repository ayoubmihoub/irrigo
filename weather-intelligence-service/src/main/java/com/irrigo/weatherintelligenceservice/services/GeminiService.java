package com.irrigo.weatherintelligenceservice.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.irrigo.taskmanagementservice.dto.TaskDTO;
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

    private final String apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent";
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Récupère des conseils d'irrigation pour une liste de tâches en un seul appel.
     */
    public Map<Long, String> getBulkAIAdvice(List<TaskDTO> tasks, WeatherInfo weather) {
        String description = (weather.getDescription() != null) ? weather.getDescription() : "Ensoleillé";

        // Construction du prompt groupé pour économiser le quota (1 appel au lieu de N)
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("En tant qu'expert agronome, donne un conseil d'irrigation très court (max 10 mots) pour chaque culture suivante. ")
                .append("Météo actuelle : ").append(description)
                .append(", Temp: ").append(String.format("%.1f", weather.getTemperature())).append("°C. ")
                .append("Réponds UNIQUEMENT sous format JSON strict comme ceci : {\"ID\": \"conseil\"}. ")
                .append("Liste des cultures :\n");

        for (TaskDTO task : tasks) {
            promptBuilder.append("- ID: ").append(task.getId()).append(", Culture: ").append(task.getCrop()).append("\n");
        }

        Map<String, Object> textPart = Map.of("text", promptBuilder.toString());
        Map<String, Object> contentItem = Map.of("parts", List.of(textPart));
        Map<String, Object> requestBody = Map.of("contents", List.of(contentItem));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-goog-api-key", apiKey);

        try {
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, entity, Map.class);

            List candidates = (List) response.getBody().get("candidates");
            if (candidates != null && !candidates.isEmpty()) {
                Map candidate = (Map) candidates.get(0);
                Map content = (Map) candidate.get("content");
                List resParts = (List) content.get("parts");
                Map part = (Map) resParts.get(0);
                String rawText = (String) part.get("text");

                // Nettoyage automatique du formatage Markdown ```json ... ```
                String jsonContent = rawText.replaceAll("```json|```", "").trim();

                return objectMapper.readValue(jsonContent, new TypeReference<Map<Long, String>>() {});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyMap();
    }

    /**
     * Méthode conservée pour la compatibilité (tâche unique).
     */
    public String getAIAdvice(String crop, WeatherInfo weather) {
        TaskDTO mockTask = new TaskDTO();
        mockTask.setId(0L);
        mockTask.setCrop(crop);

        Map<Long, String> results = getBulkAIAdvice(List.of(mockTask), weather);
        return results.getOrDefault(0L, "advice indisponible pour le moment");
    }
}