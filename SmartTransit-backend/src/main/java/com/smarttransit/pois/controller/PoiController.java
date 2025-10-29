package com.smarttransit.pois.controller;

import com.smarttransit.pois.dto.PoiDTO;
import com.smarttransit.pois.dto.PoiSearchRequest;
import com.smarttransit.pois.dto.PoiSearchResponse;
import com.smarttransit.pois.service.OverpassService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/poi")
public class PoiController {

    private final OverpassService overpassService;

    public PoiController(OverpassService overpassService) {
        this.overpassService = overpassService;
    }

    /**
     * POST /api/poi/search
     * Busca POIs cerca de una ubicación
     */
    @PostMapping("/search")
    public ResponseEntity<PoiSearchResponse> searchPOIs(@Valid @RequestBody PoiSearchRequest request) {
        try {
            long startTime = System.currentTimeMillis();

            List<PoiDTO> pois = overpassService.searchPOIs(
                request.getLatitude(),
                request.getLongitude(),
                request.getRadius(),
                request.getTypes()
            );

            // Limitar resultados si es necesario
            if (request.getLimit() != null && pois.size() > request.getLimit()) {
                pois = pois.subList(0, request.getLimit());
            }

            long searchTime = System.currentTimeMillis() - startTime;

            PoiSearchResponse response = new PoiSearchResponse(pois, pois.size(), searchTime);
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}