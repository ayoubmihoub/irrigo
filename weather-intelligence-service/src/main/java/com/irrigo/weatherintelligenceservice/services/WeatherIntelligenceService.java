package com.irrigo.weatherintelligenceservice.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.irrigo.weatherintelligenceservice.dto.TaskAdviceRequest;
import com.irrigo.weatherintelligenceservice.dto.WeatherInfo;
import com.irrigo.weatherintelligenceservice.clients.FarmServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class WeatherIntelligenceService {

    @Autowired
    private GeminiService geminiService;

    @Autowired
    private FarmServiceClient farmServiceClient;

    @Autowired
    private OpenWeatherService openWeatherService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * NOUVELLE MÉTHODE : Retourne l'état météo pour TOUTES les fermes de l'utilisateur.
     * Cette méthode boucle sur chaque ferme récupérée via Feign.
     */
    public List<Map<String, Object>> getAllFarmsWeatherStatus() {
        try {
            // 1. Récupérer toutes les fermes via le client Feign
            List<Object> allFarmsRaw = farmServiceClient.getAllFarms();
            List<Map<String, Object>> results = new ArrayList<>();

            for (Object farmObj : allFarmsRaw) {
                Map<String, Object> farm = objectMapper.convertValue(farmObj, Map.class);

                try {
                    // Extraction des données de base
                    String farmName = (String) farm.get("name");
                    String parcelJson = (String) farm.get("parcelJson");

                    // 2. Parsing du GeoJSON pour extraire les coordonnées
                    JsonNode root = objectMapper.readTree(parcelJson);
                    // On récupère le premier point du polygone [longitude, latitude]
                    JsonNode firstPoint = root.path("geometry").path("coordinates").get(0).get(0);
                    double lon = firstPoint.get(0).asDouble();
                    double lat = firstPoint.get(1).asDouble();

                    // 3. Appel à OpenWeather avec les coordonnées précises
                    WeatherInfo weatherData = openWeatherService.getFullWeather(lat, lon);

                    // Ajout au résultat final
                    results.add(Map.of(
                            "farmId", farm.get("id"),
                            "farmName", farmName,
                            "weather", weatherData
                    ));
                } catch (Exception e) {
                    // En cas d'erreur sur une ferme (ex: JSON invalide), on continue avec les autres
                    System.err.println("Erreur de traitement pour la ferme ID " + farm.get("id") + ": " + e.getMessage());
                }
            }
            return results;
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la récupération globale des fermes : " + e.getMessage());
        }
    }

    /**
     * Retourne le nom d'une ferme spécifique et sa météo (Actuelle + 3j).
     * Utilise le parcelJson pour obtenir la localisation exacte.
     */
    public Map<String, Object> getFarmWeatherStatus(Long farmId) {
        try {
            // 1. Récupération de la ferme spécifique via Feign
            Object farmObj = farmServiceClient.getFarmById(farmId);
            Map<String, Object> farm = objectMapper.convertValue(farmObj, Map.class);

            String farmName = (String) farm.get("name");
            String parcelJson = (String) farm.get("parcelJson");

            // 2. Parsing du GeoJSON
            JsonNode root = objectMapper.readTree(parcelJson);
            JsonNode firstPoint = root.path("geometry").path("coordinates").get(0).get(0);
            double lon = firstPoint.get(0).asDouble();
            double lat = firstPoint.get(1).asDouble();

            // 3. Appel OpenWeather
            WeatherInfo weatherData = openWeatherService.getFullWeather(lat, lon);

            return Map.of(
                    "farmName", farmName,
                    "weather", weatherData
            );
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la récupération du statut de la ferme " + farmId + " : " + e.getMessage());
        }
    }

    /**
     * ANCIENNE MÉTHODE CONSERVÉE : Génère un conseil personnalisé via Gemini.
     * Utilise l'humidité réelle provenant de l'ESP32.
     */
    public String getPreTaskAdvice(TaskAdviceRequest request) {
        Integer realMoisture = 0;
        try {
            // Appel Feign pour récupérer la valeur actuelle du capteur
            realMoisture = farmServiceClient.getMoisture(request.getFarmId());
            if (realMoisture == null) realMoisture = 0;
        } catch (Exception e) {
            System.err.println("Erreur récupération humidité ESP32 : " + e.getMessage());
            realMoisture = 0;
        }

        try {
            // Demande au service Gemini de générer un conseil basé sur les données réelles
            return geminiService.generateDecisionAdvice(
                    request.getCrop(),
                    request.getSoilProfile(),
                    realMoisture
            );
        } catch (Exception e) {
            return "Conseil indisponible pour le moment. Humidité actuelle détectée : " + realMoisture + "%.";
        }
    }
}