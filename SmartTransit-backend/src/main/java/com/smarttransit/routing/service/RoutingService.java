package com.smarttransit.routing.service;

import com.smarttransit.mapa.dto.GeocodingResponse;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class RoutingService {

    private static final Logger log = LoggerFactory.getLogger(RoutingService.class);

    @Value("${ors.api.key}")
    private String orsApiKey;

    @Value("${ors.api.url}")
    private String orsApiUrl;

    private final OkHttpClient httpClient;
    private long lastRequestTime = 0;

    public RoutingService() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                .build();
    }

    /**
     * Toma una coordenada GPS y la "ajusta" a la carretera más cercana usando geocoding inverso.
     * Usa Nominatim en lugar de ORS para evitar problemas con la API.
     * @param lat Latitud original.
     * @param lon Longitud original.
     * @return Un GeocodingResponse con las coordenadas validadas y contexto de ubicación.
     */
    public GeocodingResponse snapToRoad(double lat, double lon) throws IOException {
        log.info("Validating and enriching coordinates: {}, {}", lat, lon);

        // Validación básica de coordenadas
        if (lat < -90 || lat > 90 || lon < -180 || lon > 180) {
            log.error("Invalid coordinates provided: lat={}, lon={}", lat, lon);
            return null;
        }

        // Aplicar rate limiting (1 segundo entre peticiones a Nominatim)
        enforceRateLimit();

        // Usar Nominatim para obtener información de la ubicación
        // Esto proporciona contexto sin depender de ORS
        String url = String.format("https://nominatim.openstreetmap.org/reverse?lat=%.6f&lon=%.6f&format=json&zoom=18&addressdetails=1",
                lat, lon);

        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", "SmartTransit/1.0")
                .header("Accept", "application/json")
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.warn("Nominatim reverse geocoding failed with status: {}", response.code());
                // Devolver las coordenadas originales si falla
                return GeocodingResponse.builder()
                        .latitude(lat)
                        .longitude(lon)
                        .displayName("Ubicación GPS")
                        .build();
            }

            String responseBody = response.body().string();
            JSONObject json = new JSONObject(responseBody);

            // Extraer información de la ubicación
            String displayName = json.optString("display_name", "Ubicación GPS");
            
            // Usar las coordenadas retornadas por Nominatim (más precisas)
            double adjustedLat = json.optDouble("lat", lat);
            double adjustedLon = json.optDouble("lon", lon);

            log.info("Validated coordinates: {}, {} -> {}", lat, lon, displayName);

            return GeocodingResponse.builder()
                    .latitude(adjustedLat)
                    .longitude(adjustedLon)
                    .displayName(displayName)
                    .build();
                    
        } catch (Exception e) {
            log.error("Error during snap-to-road operation: {}", e.getMessage());
            // En caso de error, devolver las coordenadas originales
            return GeocodingResponse.builder()
                    .latitude(lat)
                    .longitude(lon)
                    .displayName("Ubicación GPS")
                    .build();
        }
    }

    /**
     * Aplica rate limiting para respetar las políticas de uso de Nominatim.
     * Espera al menos 1 segundo entre peticiones consecutivas.
     */
    private void enforceRateLimit() {
        long currentTime = System.currentTimeMillis();
        long timeSinceLastRequest = currentTime - lastRequestTime;
        if (timeSinceLastRequest < 1000) {
            try {
                Thread.sleep(1000 - timeSinceLastRequest);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Rate limiting interrupted");
            }
        }
        lastRequestTime = System.currentTimeMillis();
    }
}