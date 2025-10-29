package com.smarttransit.routing.controller;

import com.smarttransit.mapa.dto.GeocodingResponse;
import com.smarttransit.routing.service.RoutingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/routing")
public class RoutingController {

    private final RoutingService routingService;

    public RoutingController(RoutingService routingService) {
        this.routingService = routingService;
    }

    /**
     * GET /api/routing/snap?lat=-15.49&lon=-70.13
     * Valida y enriquece una coordenada GPS con información de ubicación.
     * Acepta tanto lat/lon como latitude/longitude para compatibilidad.
     */
    @GetMapping("/snap")
    public ResponseEntity<GeocodingResponse> snapToRoad(
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lon,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude) {
        
        try {
            // Aceptar ambos formatos de parámetros
            double finalLat = (latitude != null) ? latitude : (lat != null ? lat : 0.0);
            double finalLon = (longitude != null) ? longitude : (lon != null ? lon : 0.0);
            
            if (finalLat == 0.0 && finalLon == 0.0) {
                return ResponseEntity.badRequest().build();
            }
            
            GeocodingResponse snappedLocation = routingService.snapToRoad(finalLat, finalLon);
            return snappedLocation != null ? ResponseEntity.ok(snappedLocation) : ResponseEntity.notFound().build();
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            return ResponseEntity.status(503).build(); // Service Unavailable
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}