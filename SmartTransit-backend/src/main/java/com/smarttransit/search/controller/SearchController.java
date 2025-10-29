package com.smarttransit.search.controller;

import com.smarttransit.mapa.dto.GeocodingResponse;
import com.smarttransit.search.service.PhotonService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/search")
public class SearchController {

    private final PhotonService photonService;

    public SearchController(PhotonService photonService) {
        this.photonService = photonService;
    }

    /**
     * GET /api/search/autocomplete?q=pizza&lat=-15.49&lon=-70.13
     * Endpoint para el autocompletado de la barra de búsqueda.
     */
    @GetMapping("/autocomplete")
    public ResponseEntity<List<GeocodingResponse>> autocomplete(
            @RequestParam String q,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lon,
            @RequestParam(defaultValue = "10") int limit
    ) {
        try {
            List<GeocodingResponse> results = photonService.search(q, lat, lon, limit);
            return ResponseEntity.ok(results);
        } catch (IOException e) {
            // En caso de error con la API externa, devolvemos un error de servidor.
            return ResponseEntity.internalServerError().build();
        }
    }
}