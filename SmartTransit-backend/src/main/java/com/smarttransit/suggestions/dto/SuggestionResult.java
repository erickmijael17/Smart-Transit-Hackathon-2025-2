package com.smarttransit.suggestions.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Resultado completo de la búsqueda de sugerencias
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuggestionResult {
    
    /**
     * Ubicación actual del usuario
     */
    private LocationInfo currentLocation;
    
    /**
     * Destino del usuario
     */
    private LocationInfo destination;
    
    /**
     * Lista de rutas sugeridas ordenadas por relevancia
     */
    private List<RouteSuggestionResponse> suggestions;
    
    /**
     * Información adicional sobre la búsqueda
     */
    private String message;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LocationInfo {
        private Double latitude;
        private Double longitude;
        private String displayName;
    }
}
