package com.irrigo.weatherintelligenceservice.services;

import com.irrigo.weatherintelligenceservice.dto.WeatherInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OpenWeatherService {

    @Value("${api.openweather.key}")
    private String apiKey;

    private final String forecastUrl = "https://api.openweathermap.org/data/2.5/forecast";
    private final RestTemplate restTemplate = new RestTemplate();

    public WeatherInfo getFullWeather(double lat, double lon) {
        String url = String.format("%s?lat=%s&lon=%s&appid=%s&units=metric&lang=fr", forecastUrl, lat, lon, apiKey);

        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            List<Map<String, Object>> list = (List<Map<String, Object>>) response.get("list");
            Map<String, Object> cityData = (Map<String, Object>) response.get("city");

            // 1. Météo Actuelle
            Map<String, Object> now = list.get(0);
            Map<String, Object> mainNow = (Map<String, Object>) now.get("main");
            List<Map<String, Object>> weatherDetails = (List<Map<String, Object>>) now.get("weather");

            WeatherInfo.CurrentWeather current = WeatherInfo.CurrentWeather.builder()
                    .temperature(Double.parseDouble(mainNow.get("temp").toString()))
                    .humidity(Integer.parseInt(mainNow.get("humidity").toString()))
                    .description(weatherDetails.get(0).get("description").toString())
                    // Conversion de la probabilité en %
                    .rainProbability((int) (Double.parseDouble(now.get("pop").toString()) * 100))
                    .build();

            // 2. Prévisions sur 3 jours (filtrage sur 12:00:00)
            List<WeatherInfo.ForecastDay> forecast = list.stream()
                    .filter(item -> item.get("dt_txt").toString().contains("12:00:00"))
                    .skip(1)
                    .limit(3)
                    .map(item -> {
                        Map<String, Object> m = (Map<String, Object>) item.get("main");
                        return WeatherInfo.ForecastDay.builder()
                                .date(item.get("dt_txt").toString().split(" ")[0])
                                .tempMin(Double.parseDouble(m.get("temp_min").toString()))
                                .tempMax(Double.parseDouble(m.get("temp_max").toString()))
                                .description(((List<Map<String, Object>>) item.get("weather")).get(0).get("description").toString())
                                // Conversion de la probabilité en %
                                .rainProbability((int) (Double.parseDouble(item.get("pop").toString()) * 100))
                                .build();
                    })
                    .collect(Collectors.toList());

            return WeatherInfo.builder()
                    .cityName(cityData.get("name").toString())
                    .current(current)
                    .forecast(forecast)
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Erreur OpenWeather : " + e.getMessage());
        }
    }
}