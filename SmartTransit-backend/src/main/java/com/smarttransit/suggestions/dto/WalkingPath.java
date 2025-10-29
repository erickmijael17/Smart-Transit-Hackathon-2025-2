package com.smarttransit.suggestions.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Representa un trazado de caminata en el mapa
 * Guía visual para el usuario desde un punto a otro
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalkingPath {
    
    /**
     * Tipo de trazado
     */
    private PathType type;
    
    /**
     * Coordenadas del trazado [[lat, lon], [lat, lon], ...]
     * Formato compatible con Leaflet/Mapbox
     */
    private List<List<Double>> coordinates;
    
    /**
     * Distancia del trazado en metros
     */
    private Double distance;
    
    /**
     * Tiempo estimado de caminata en minutos
     */
    private Integer estimatedWalkingTime;
    
    /**
     * Descripción del trazado
     */
    private String description;
    
    /**
     * Color sugerido para el trazado en el mapa
     */
    private String color;
    
    /**
     * Indica si el trazado sigue calles reales (true) o es una línea recta (false)
     */
    private Boolean followsRoads;
    
    /**
     * Tipo de trazado
     */
    public enum PathType {
        /** Desde ubicación actual hasta parada más cercana */
        USER_TO_STOP,
        
        /** Desde parada final hasta destino */
        STOP_TO_DESTINATION
    }
}
