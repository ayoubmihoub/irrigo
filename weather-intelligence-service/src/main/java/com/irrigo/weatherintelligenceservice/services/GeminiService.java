package com.irrigo.weatherintelligenceservice.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    @Value("${api.gemini.key}")
    private String apiKey;

    private static final String API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";

    private final RestTemplate restTemplate = new RestTemplate();

    public String generateDecisionAdvice(String crop,
                                         String soil,
                                         int currentMoisture) {

        String prompt = String.format(
                """
                Tu es un agronome expert en irrigation.

                Culture : %s
                Type de sol : %s
                Humidité actuelle : %d%%

                Réponds uniquement sous la forme :

                OUI, vous pouvez irriguer - justification courte

                ou

                NON, attendez encore - justification courte
                """,
                crop,
                soil,
                currentMoisture
        );

        return callGemini(prompt, currentMoisture);
    }

    private String callGemini(String prompt, int currentMoisture) {

        String url = API_URL + "?key=" + apiKey;

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of(
                                "parts", List.of(
                                        Map.of("text", prompt)
                                )
                        )
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request =
                new HttpEntity<>(requestBody, headers);

        int maxRetries = 3;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {

            try {

                ResponseEntity<Map> response =
                        restTemplate.exchange(
                                url,
                                HttpMethod.POST,
                                request,
                                Map.class
                        );

                Map<String, Object> body = response.getBody();

                if (body == null) {
                    return "Réponse Gemini vide.";
                }

                List<?> candidates =
                        (List<?>) body.get("candidates");

                if (candidates == null || candidates.isEmpty()) {
                    return "Aucune réponse générée.";
                }

                Map<?, ?> candidate =
                        (Map<?, ?>) candidates.get(0);

                Map<?, ?> content =
                        (Map<?, ?>) candidate.get("content");

                if (content == null) {
                    return "Contenu Gemini introuvable.";
                }

                List<?> parts =
                        (List<?>) content.get("parts");

                if (parts == null || parts.isEmpty()) {
                    return "Réponse Gemini invalide.";
                }

                Map<?, ?> firstPart =
                        (Map<?, ?>) parts.get(0);

                Object text = firstPart.get("text");

                if (text == null) {
                    return "Texte Gemini introuvable.";
                }

                return text.toString();

            } catch (HttpStatusCodeException e) {

                System.err.println("Erreur Gemini : "
                        + e.getStatusCode());
                System.err.println(e.getResponseBodyAsString());

                if (e.getStatusCode().value() == 503
                        && attempt < maxRetries) {

                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }

                    continue;
                }

                return "Erreur Gemini : "
                        + e.getStatusCode().value();

            } catch (Exception e) {

                e.printStackTrace();

                return "Service Gemini indisponible.";
            }
        }

        return "Service Gemini temporairement indisponible.";
    }
}