package com.smarttransit.suggestions.service;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Servicio para obtener rutas peatonales reales que siguen las calles
 * Usa OSRM (Open Source Routing Machine) - API pública y gratuita
 */
@Service
public class WalkingRouteService {
    
    private static final Logger log = LoggerFactory.getLogger(WalkingRouteService.class);
    
    // OSRM público y gratuito
    private static final String OSRM_API_URL = "https://router.project-osrm.org/route/v1/foot";
    
    private final OkHttpClient httpClient;
    private long lastRequestTime = 0;
    
    public WalkingRouteService() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build();
    }
    
    /**
     * Obtiene una ruta peatonal real entre dos puntos que sigue las calles
     * @param startLat Latitud de inicio
     * @param startLon Longitud de inicio
     * @param endLat Latitud de destino
     * @param endLon Longitud de destino
     * @return WalkingRouteResult con coordenadas que siguen calles, distancia y tiempo
     */
    public WalkingRouteResult getWalkingRoute(double startLat, double startLon, 
                                               double endLat, double endLon) {
        try {
            // Rate limiting (respetar uso justo de API pública)
            enforceRateLimit();
            
            // Construir URL: /route/v1/{profile}/{coordinates}
            // Coordenadas en formato: lon,lat;lon,lat (OSRM usa lon,lat!)
            String url = String.format("%s/%.6f,%.6f;%.6f,%.6f?overview=full&geometries=geojson",
                                      OSRM_API_URL, startLon, startLat, endLon, endLat);
            
            log.debug("Requesting walking route: ({},{}) -> ({},{})", 
                     startLat, startLon, endLat, endLat);
            
            Request request = new Request.Builder()
                    .url(url)
                    .header("User-Agent", "SmartTransit/1.0")
                    .get()
                    .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.warn("OSRM API error: {}", response.code());
                    return createFallbackRoute(startLat, startLon, endLat, endLon);
                }
                
                String responseBody = response.body().string();
                return parseOSRMResponse(responseBody, startLat, startLon, endLat, endLon);
            }
            
        } catch (Exception e) {
            log.error("Error getting walking route: {}", e.getMessage());
            return createFallbackRoute(startLat, startLon, endLat, endLon);
        }
    }
    
    /**
     * Parsea la respuesta de OSRM y extrae coordenadas, distancia y duración
     */
    private WalkingRouteResult parseOSRMResponse(String responseBody,
                                                  double startLat, double startLon,
                                                  double endLat, double endLon) {
        try {
            JSONObject json = new JSONObject(responseBody);
            
            // Verificar si la respuesta es válida
            String code = json.optString("code", "");
            if (!"Ok".equals(code)) {
                log.warn("OSRM response code: {}", code);
                return createFallbackRoute(startLat, startLon, endLat, endLon);
            }
            
            JSONArray routes = json.optJSONArray("routes");
            if (routes == null || routes.length() == 0) {
                log.warn("No routes found in OSRM response");
                return createFallbackRoute(startLat, startLon, endLat, endLon);
            }
            
            JSONObject route = routes.getJSONObject(0);
            
            // Extraer distancia (en metros) y duración (en segundos)
            double distance = route.optDouble("distance", 0.0);
            double duration = route.optDouble("duration", 0.0);
            
            // Extraer geometría (coordenadas del trazado)
            JSONObject geometry = route.optJSONObject("geometry");
            List<List<Double>> coordinates = new ArrayList<>();
            
            if (geometry != null && geometry.has("coordinates")) {
                JSONArray coords = geometry.getJSONArray("coordinates");
                
                for (int i = 0; i < coords.length(); i++) {
                    JSONArray coord = coords.getJSONArray(i);
                    // OSRM devuelve [lon, lat], necesitamos [lat, lon] para Leaflet
                    double lon = coord.getDouble(0);
                    double lat = coord.getDouble(1);
                    coordinates.add(Arrays.asList(lat, lon));
                }
            }
            
            // Si no hay coordenadas, usar fallback
            if (coordinates.isEmpty()) {
                return createFallbackRoute(startLat, startLon, endLat, endLon);
            }
            
            int durationMinutes = (int) Math.ceil(duration / 60.0);
            
            log.debug("Walking route found: {} points, {}m, {} min", 
                     coordinates.size(), Math.round(distance), durationMinutes);
            
            return WalkingRouteResult.builder()
                    .coordinates(coordinates)
                    .distance(distance)
                    .duration(durationMinutes)
                    .followsRoads(true)
                    .build();
            
        } catch (Exception e) {
            log.error("Error parsing OSRM response: {}", e.getMessage());
            return createFallbackRoute(startLat, startLon, endLat, endLon);
        }
    }
    
    /**
     * Crea una ruta de respaldo (línea recta) cuando OSRM falla
     */
    private WalkingRouteResult createFallbackRoute(double startLat, double startLon,
                                                     double endLat, double endLon) {
        log.debug("Using fallback straight-line route");
        
        List<List<Double>> coordinates = new ArrayList<>();
        coordinates.add(Arrays.asList(startLat, startLon));
        coordinates.add(Arrays.asList(endLat, endLon));
        
        // Calcular distancia haversine como fallback
        double distance = calculateHaversineDistance(startLat, startLon, endLat, endLon);
        int duration = (int) Math.ceil(distance / 83.33); // 5 km/h = 83.33 m/min
        
        return WalkingRouteResult.builder()
                .coordinates(coordinates)
                .distance(distance)
                .duration(duration)
                .followsRoads(false) // Indica que es línea recta
                .build();
    }
    
    /**
     * Cálculo de distancia Haversine para fallback
     */
    private double calculateHaversineDistance(double lat1, double lon1, 
                                               double lat2, double lon2) {
        final double R = 6371000; // Radio de la Tierra en metros
        
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double deltaLat = Math.toRadians(lat2 - lat1);
        double deltaLon = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                   Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                   Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c;
    }
    
    /**
     * Rate limiting para ser un buen ciudadano de la API pública
     * Espera 200ms entre peticiones
     */
    private void enforceRateLimit() {
        long currentTime = System.currentTimeMillis();
        long timeSinceLastRequest = currentTime - lastRequestTime;
        
        if (timeSinceLastRequest < 200) {
            try {
                Thread.sleep(200 - timeSinceLastRequest);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        lastRequestTime = System.currentTimeMillis();
    }
    
    /**
     * Resultado de una ruta peatonal
     */
    public static class WalkingRouteResult {
        private List<List<Double>> coordinates;  // [[lat, lon], ...]
        private double distance;                  // metros
        private int duration;                     // minutos
        private boolean followsRoads;             // true si sigue calles reales
        
        public static Builder builder() {
            return new Builder();
        }
        
        public static class Builder {
            private WalkingRouteResult result = new WalkingRouteResult();
            
            public Builder coordinates(List<List<Double>> coordinates) {
                result.coordinates = coordinates;
                return this;
            }
            
            public Builder distance(double distance) {
                result.distance = distance;
                return this;
            }
            
            public Builder duration(int duration) {
                result.duration = duration;
                return this;
            }
            
            public Builder followsRoads(boolean followsRoads) {
                result.followsRoads = followsRoads;
                return this;
            }
            
            public WalkingRouteResult build() {
                return result;
            }
        }
        
        // Getters
        public List<List<Double>> getCoordinates() { return coordinates; }
        public double getDistance() { return distance; }
        public int getDuration() { return duration; }
        public boolean isFollowsRoads() { return followsRoads; }
    }
}
