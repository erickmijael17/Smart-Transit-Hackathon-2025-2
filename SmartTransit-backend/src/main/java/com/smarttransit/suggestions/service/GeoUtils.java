package com.smarttransit.suggestions.service;

import org.springframework.stereotype.Component;

/**
 * Utilidades para cálculos geográficos
 */
@Component
public class GeoUtils {
    
    private static final double EARTH_RADIUS_KM = 6371.0;
    private static final double EARTH_RADIUS_M = 6371000.0;
    
    /**
     * Calcula la distancia entre dos puntos geográficos usando la fórmula de Haversine
     * @param lat1 Latitud del punto 1
     * @param lon1 Longitud del punto 1
     * @param lat2 Latitud del punto 2
     * @param lon2 Longitud del punto 2
     * @return Distancia en metros
     */
    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // Convertir grados a radianes
        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double lat2Rad = Math.toRadians(lat2);
        double lon2Rad = Math.toRadians(lon2);
        
        // Diferencias
        double dLat = lat2Rad - lat1Rad;
        double dLon = lon2Rad - lon1Rad;
        
        // Fórmula de Haversine
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return EARTH_RADIUS_M * c;
    }
    

    
    /**
     * Calcula la distancia mínima desde un punto a una polilínea (conjunto de puntos)
     * @param lat Latitud del punto
     * @param lon Longitud del punto
     * @param polyline Lista de coordenadas [[lat, lon], ...]
     * @return Distancia mínima en metros
     */
    public double distanceToPolyline(double lat, double lon, java.util.List<java.util.List<Double>> polyline) {
        if (polyline == null || polyline.isEmpty()) {
            return Double.MAX_VALUE;
        }
        
        double minDistance = Double.MAX_VALUE;
        
        for (java.util.List<Double> point : polyline) {
            if (point.size() >= 2) {
                double distance = calculateDistance(lat, lon, point.get(0), point.get(1));
                minDistance = Math.min(minDistance, distance);
            }
        }
        
        return minDistance;
    }
    
    /**
     * Normaliza un valor de distancia a un score de 0-100
     * Distancias más cortas = scores más altos
     * @param distanceMeters Distancia en metros
     * @param maxDistanceMeters Distancia máxima para normalización (default: 5000m)
     * @return Score de 0 a 100
     */
    public double distanceToScore(double distanceMeters, double maxDistanceMeters) {
        if (distanceMeters >= maxDistanceMeters) {
            return 0.0;
        }
        return 100.0 * (1.0 - (distanceMeters / maxDistanceMeters));
    }
    
    /**
     * Normaliza un valor de distancia a un score de 0-100 con distancia máxima default de 5km
     */
    public double distanceToScore(double distanceMeters) {
        return distanceToScore(distanceMeters, 5000.0);
    }
}
