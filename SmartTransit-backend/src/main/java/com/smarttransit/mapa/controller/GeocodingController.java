package com.smarttransit.mapa.controller;

import com.smarttransit.mapa.dto.GeocodingRequest;
import com.smarttransit.mapa.dto.GeocodingResponse;
import com.smarttransit.mapa.service.GeocodingService;
import jakarta.validation.Valid;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/geocoding")
public class GeocodingController {

    private final GeocodingService geocodingService;

    public GeocodingController(GeocodingService geocodingService) {
        this.geocodingService = geocodingService;
    }

    /**
     * POST /api/geocoding/search
     * Geocoding: convierte dirección a coordenadas
     */
    @PostMapping("/search")
    public ResponseEntity<GeocodingResponse> geocode(@Valid @RequestBody GeocodingRequest request) throws IOException {
        JSONObject result = geocodingService.geocode(request.getAddress());

        if (result == null) {
            return ResponseEntity.notFound().build();
        }

        GeocodingResponse response = GeocodingResponse.fromJSON(result);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/geocoding/reverse?latitude={lat}&longitude={lon}
     * Reverse Geocoding: convierte coordenadas a dirección
     * Acepta tanto lat/lon como latitude/longitude para compatibilidad
     */
    @GetMapping("/reverse")
    public ResponseEntity<GeocodingResponse> reverseGeocode(
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lon,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude) throws IOException {
        
        // Aceptar ambos formatos de parámetros
        double finalLat = (latitude != null) ? latitude : (lat != null ? lat : 0.0);
        double finalLon = (longitude != null) ? longitude : (lon != null ? lon : 0.0);
        
        if (finalLat == 0.0 && finalLon == 0.0) {
            return ResponseEntity.badRequest().build();
        }
        
        JSONObject result = geocodingService.reverseGeocode(finalLat, finalLon);
        if (result == null) {
            return ResponseEntity.notFound().build();
        }
        
        GeocodingResponse response = GeocodingResponse.fromJSON(result);
        return ResponseEntity.ok(response);
    }
}