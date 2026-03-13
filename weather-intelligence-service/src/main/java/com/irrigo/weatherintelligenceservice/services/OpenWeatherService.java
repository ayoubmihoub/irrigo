package com.irrigo.weatherintelligenceservice.services;

import com.irrigo.weatherintelligenceservice.dto.WeatherInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Map;
import java.util.List;

@Service
public class OpenWeatherService {

    @Value("${api.openweather.key}")
    private String apiKey;

    private final String apiUrl = "https://api.openweathermap.org/data/2.5/weather";
    private final RestTemplate restTemplate = new RestTemplate();

    public WeatherInfo getWeather(String location) {
        String url;
        // Vérifie si la location contient une virgule (format lat, lon)
        if (location.contains(",")) {
            String[] coords = location.split(",");
            String lat = coords[0].trim();
            String lon = coords[1].trim();
            url = String.format("%s?lat=%s&lon=%s&appid=%s&units=metric&lang=fr", apiUrl, lat, lon, apiKey);
        } else {
            url = String.format("%s?q=%s&appid=%s&units=metric&lang=fr", apiUrl, location, apiKey);
        }

        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            WeatherInfo info = new WeatherInfo();

            // OpenWeather retourne le nom du lieu trouvé dans le champ "name"
            info.setCityName(response.get("name").toString());

            Map<String, Object> main = (Map<String, Object>) response.get("main");
            info.setTemperature(Double.parseDouble(main.get("temp").toString()));
            info.setHumidity(Integer.parseInt(main.get("humidity").toString()));

            List<Map<String, Object>> weatherList = (List<Map<String, Object>>) response.get("weather");
            if (!weatherList.isEmpty()) {
                info.setDescription(weatherList.get(0).get("description").toString());
            }

            Map<String, Object> clouds = (Map<String, Object>) response.get("clouds");
            info.setRainProbability(Double.parseDouble(clouds.get("all").toString()) / 100.0);

            return info;
        } catch (Exception e) {
            throw new RuntimeException("Erreur OpenWeather : " + e.getMessage());
        }
    }
}