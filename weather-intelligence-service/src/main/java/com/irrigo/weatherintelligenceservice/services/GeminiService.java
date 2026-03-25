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

    // URL mise à jour selon ton cURL fonctionnel
    private final String apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent";
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Analyse pour le tableau de bord (Bulk)
     */
    public Map<Long, String> getBulkAIAdvice(List<TaskDTO> tasks, WeatherInfo weather, Map<Long, Long> plantAges) {
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("Tu es un expert agronome. Donne un conseil d'irrigation (max 30 mots) pour chaque cas. ")
                .append("Météo : ").append(weather.getTemperature()).append("°C, ")
                .append("Humidité: ").append(weather.getHumidity()).append("%, ")
                .append("Pluie: ").append(weather.getRainProbability()).append("%. ")
                .append("Réponds UNIQUEMENT en JSON strict : {\"ID\": \"conseil\"}.\n");

        for (TaskDTO task : tasks) {
            promptBuilder.append("- ID: ").append(task.getId())
                    .append(", Culture: ").append(task.getCrop())
                    .append(", Sol: ").append(task.getSoilProfile())
                    .append(", Âge: ").append(plantAges.getOrDefault(task.getId(), 0L)).append(" jours\n");
        }

        return callGeminiInternal(promptBuilder.toString());
    }

    /**
     * Analyse pour une tâche unique (Détail)
     */
    public String generateExpertAdvice(String crop, String soil, long age, WeatherInfo weather) {
        String prompt = String.format(
                "Expert irrigation. Analyse : Culture %s (%d j), Sol %s, Météo %.1f°C, Humidité %d%%, Pluie %.1f%%. " +
                        "Donne un conseil court. Réponds en JSON : {\"0\": \"conseil\"}",
                crop, age, soil, weather.getTemperature(), weather.getHumidity(), weather.getRainProbability()
        );

        Map<Long, String> result = callGeminiInternal(prompt);
        return result.getOrDefault(0L, "Conseil indisponible");
    }

    /**
     * Méthode générique qui respecte la structure de ton cURL
     */
    private Map<Long, String> callGeminiInternal(String promptText) {
        // Structure identique à ton cURL : contents -> parts -> text
        Map<String, Object> textPart = Map.of("text", promptText);
        Map<String, Object> contentItem = Map.of("parts", List.of(textPart));
        Map<String, Object> requestBody = Map.of("contents", List.of(contentItem));

        // Headers avec ta clé API
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-goog-api-key", apiKey);

        try {
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, entity, Map.class);

            // Extraction sécurisée selon la structure Gemini
            List candidates = (List) response.getBody().get("candidates");
            if (candidates != null && !candidates.isEmpty()) {
                Map candidate = (Map) candidates.get(0);
                Map content = (Map) candidate.get("content");
                List resParts = (List) content.get("parts");
                String rawText = (String) ((Map) resParts.get(0)).get("text");

                // Nettoyage Markdown (au cas où l'IA en ajoute)
                String jsonContent = rawText.replaceAll("```json|```", "").trim();
                return objectMapper.readValue(jsonContent, new TypeReference<Map<Long, String>>() {});
            }
        } catch (Exception e) {
            System.err.println("Erreur technique Gemini : " + e.getMessage());
        }
        return Collections.emptyMap();
    }
}