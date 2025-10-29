package com.smarttransit.suggestions.controller;

import com.smarttransit.suggestions.dto.RouteSuggestionRequest;
import com.smarttransit.suggestions.dto.SuggestionResult;
import com.smarttransit.suggestions.service.RouteSuggestionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * Controlador REST para sugerencias de rutas de transporte público
 */
@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/suggestions")
public class RouteSuggestionController {
    
    private static final Logger log = LoggerFactory.getLogger(RouteSuggestionController.class);
    
    private final RouteSuggestionService suggestionService;
    
    public RouteSuggestionController(RouteSuggestionService suggestionService) {
        this.suggestionService = suggestionService;
    }
    
    /**
     * POST /api/suggestions/routes
     * Obtiene sugerencias de rutas basadas en ubicación actual y destino
     * 
     * Request Body:
     * {
     *   "currentLatitude": -15.494655,
     *   "currentLongitude": -70.154834,
     *   "destination": "centro",
     *   "maxSuggestions": 3
     * }
     */
    @PostMapping("/routes")
    public ResponseEntity<SuggestionResult> getRouteSuggestions(@RequestBody RouteSuggestionRequest request) {
        try {
            log.info("Received suggestion request: from ({}, {}) to '{}'",
                     request.getCurrentLatitude(), request.getCurrentLongitude(), request.getDestination());
            
            SuggestionResult result = suggestionService.getSuggestions(request);
            return ResponseEntity.ok(result);
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            log.error("Error calling external geocoding service: {}", e.getMessage());
            return ResponseEntity.status(503).build(); // Service Unavailable
        } catch (Exception e) {
            log.error("Unexpected error generating suggestions", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * GET /api/suggestions/routes
     * Versión GET del endpoint que acepta múltiples formatos de parámetros
     * 
     * Formato 1 (Frontend actual):
     * - originLat, originLon: ubicación actual
     * - destLat, destLon: coordenadas del destino
     * - maxResults: máximo de sugerencias (opcional, default: 3)
     * - maxWalkingDistance: distancia máxima de caminata en metros (opcional)
     * 
     * Formato 2 (Texto):
     * - lat, lon: ubicación actual
     * - destination: destino en texto
     * - limit: máximo de sugerencias (opcional, default: 3)
     */
    @GetMapping("/routes")
    public ResponseEntity<SuggestionResult> getRouteSuggestionsGet(
            // Formato 1: Coordenadas directas (frontend)
            @RequestParam(required = false) Double originLat,
            @RequestParam(required = false) Double originLon,
            @RequestParam(required = false) Double destLat,
            @RequestParam(required = false) Double destLon,
            @RequestParam(required = false) Integer maxResults,
            @RequestParam(required = false) Double maxWalkingDistance,
            
            // Formato 2: Texto de destino (legacy/testing)
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lon,
            @RequestParam(required = false) String destination,
            @RequestParam(required = false) Integer limit) {
        
        RouteSuggestionRequest request = new RouteSuggestionRequest();
        
        // Determinar qué formato se está usando
        if (originLat != null && originLon != null) {
            // Formato 1: Frontend con coordenadas
            log.info("GET request (coordinates format): origin=({}, {}), dest=({}, {})", 
                     originLat, originLon, destLat, destLon);
            
            request.setCurrentLatitude(originLat);
            request.setCurrentLongitude(originLon);
            request.setDestinationLatitude(destLat);
            request.setDestinationLongitude(destLon);
            request.setMaxSuggestions(maxResults != null ? maxResults : 3);
            request.setMaxWalkingDistance(maxWalkingDistance);
            
        } else if (lat != null && lon != null && destination != null) {
            // Formato 2: Legacy con texto
            log.info("GET request (text format): origin=({}, {}), destination='{}'", 
                     lat, lon, destination);
            
            request.setCurrentLatitude(lat);
            request.setCurrentLongitude(lon);
            request.setDestination(destination);
            request.setMaxSuggestions(limit != null ? limit : 3);
            
        } else {
            log.warn("Invalid GET request: missing required parameters");
            return ResponseEntity.badRequest().build();
        }
        
        return getRouteSuggestions(request);
    }
}
