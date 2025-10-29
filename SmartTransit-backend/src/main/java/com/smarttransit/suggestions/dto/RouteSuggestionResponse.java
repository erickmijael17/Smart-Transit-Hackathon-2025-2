package com.smarttransit.suggestions.dto;

import com.smarttransit.transito.dto.StopDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Respuesta con la sugerencia de ruta
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteSuggestionResponse {
    
    /**
     * ID de la ruta sugerida
     */
    private String routeId;
    
    /**
     * Nombre de la ruta
     */
    private String routeName;
    
    /**
     * Color de la ruta
     */
    private String routeColor;
    
    /**
     * Puntuación de relevancia (0-100)
     */
    private Double score;
    
    /**
     * Parada más cercana al usuario
     */
    private StopDTO nearestStopToUser;
    
    /**
     * Distancia a la parada más cercana (en metros)
     */
    private Double distanceToNearestStop;
    
    /**
     * Parada más cercana al destino
     */
    private StopDTO nearestStopToDestination;
    
    /**
     * Distancia desde la última parada al destino (en metros)
     */
    private Double distanceFromRouteToDestination;
    
    /**
     * Razón de la sugerencia
     */
    private String reason;
    
    /**
     * Trazados de caminata recomendados
     * Incluye: usuario → parada y parada → destino
     */
    private List<WalkingPath> walkingPaths;
}
