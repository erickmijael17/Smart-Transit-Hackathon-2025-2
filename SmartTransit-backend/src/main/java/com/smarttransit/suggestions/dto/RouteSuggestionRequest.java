package com.smarttransit.suggestions.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request para solicitar sugerencias de rutas
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouteSuggestionRequest {
    
    /**
     * Ubicación actual del usuario (latitud)
     */
    private Double currentLatitude;
    
    /**
     * Ubicación actual del usuario (longitud)
     */
    private Double currentLongitude;
    
    /**
     * Destino en texto plano (ej: "centro", "plaza armas", "salida puno")
     * Opcional si se proporcionan destinationLatitude y destinationLongitude
     */
    private String destination;
    
    /**
     * Latitud del destino (opcional, alternativa a destination)
     */
    private Double destinationLatitude;
    
    /**
     * Longitud del destino (opcional, alternativa a destination)
     */
    private Double destinationLongitude;
    
    /**
     * Nombre del destino (opcional, para mostrar en UI)
     */
    private String destinationName;
    
    /**
     * Número máximo de sugerencias a retornar (default: 3)
     */
    private Integer maxSuggestions = 3;
    
    /**
     * Distancia máxima de caminata en metros (opcional)
     */
    private Double maxWalkingDistance;
}
