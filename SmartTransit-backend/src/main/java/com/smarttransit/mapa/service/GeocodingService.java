package com.smarttransit.mapa.service;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@Service
public class GeocodingService {

    private static final Logger log = LoggerFactory.getLogger(GeocodingService.class);

    @Value("${nominatim.base.url}")
    private String baseUrl;

    @Value("${nominatim.user.agent}")
    private String userAgent;

    @Value("${nominatim.timeout.seconds}")
    private int timeoutSeconds;

    @Value("${nominatim.rate.limit.enabled}")
    private boolean rateLimitEnabled;

    private final OkHttpClient httpClient;
    private long lastRequestTime = 0;

    public GeocodingService() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build();
    }

    @Cacheable(value = "geocoding", key = "#address")
    public JSONObject geocode(String address) throws IOException {
        enforceRateLimit();

        String encodedAddress = URLEncoder.encode(address, StandardCharsets.UTF_8.toString());
        String url = String.format("%s/search?q=%s&format=json&limit=1&addressdetails=1",
                baseUrl, encodedAddress);

        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", userAgent)
                .build();

        log.info("Geocoding request: {}", address);

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Nominatim API error: " + response.code() + " " + response.message());
            }

            String responseBody = response.body().string();
            JSONArray results = new JSONArray(responseBody);

            if (results.length() == 0) {
                log.warn("No results found for address: {}", address);
                return null;
            }

            JSONObject result = results.getJSONObject(0);
            log.info("Geocoded {} to ({}, {})", address,
                    result.getString("lat"), result.getString("lon"));

            return result;
        }
    }

    /**
     * Reverse Geocoding: Convierte coordenadas a dirección
     * @param lat Latitud
     * @param lon Longitud
     * @return JSONObject con address components, o null si falla
     */
    @Cacheable(value = "reverse-geocoding", key = "#lat + ',' + #lon")
    public JSONObject reverseGeocode(double lat, double lon) throws IOException {
        enforceRateLimit();

        if (lat < -90 || lat > 90 || lon < -180 || lon > 180) {
            log.error("Invalid coordinates provided: lat={}, lon={}", lat, lon);
            throw new IllegalArgumentException("Invalid coordinates provided.");
        }

        String url = String.format("%s/reverse?lat=%.6f&lon=%.6f&format=json&zoom=18&addressdetails=1",
                baseUrl, lat, lon);

        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", userAgent)
                .header("Accept", "application/json")
                .build();

        log.info("Reverse geocoding request: ({}, {})", lat, lon);

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.warn("Nominatim API error: {} {}", response.code(), response.message());
                
                // Si es error de rate limiting, lanzar excepción
                if (response.code() == 429) {
                    throw new IOException("Rate limit exceeded. Please try again later.");
                }
                
                // Para otros errores, retornar null
                return null;
            }

            String responseBody = response.body().string();
            
            // Validar que no esté vacío
            if (responseBody == null || responseBody.trim().isEmpty()) {
                log.warn("Empty response from Nominatim API");
                return null;
            }
            
            JSONObject result = new JSONObject(responseBody);
            
            // Verificar que tenga información útil
            if (!result.has("display_name") && !result.has("address")) {
                log.warn("No location data found for coordinates: ({}, {})", lat, lon);
                return null;
            }

            log.info("Reverse geocoded ({}, {}) to {}",
                    lat, lon, result.optString("display_name", "unknown"));

            return result;
            
        } catch (IOException e) {
            log.error("IOException during reverse geocoding: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during reverse geocoding: {}", e.getMessage(), e);
            return null;
        }
    }

    private void enforceRateLimit() {
        if (!rateLimitEnabled) return;
        long currentTime = System.currentTimeMillis();
        long timeSinceLastRequest = currentTime - lastRequestTime;
        if (timeSinceLastRequest < 1000) {
            try {
                Thread.sleep(1000 - timeSinceLastRequest);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        lastRequestTime = System.currentTimeMillis();
    }
}