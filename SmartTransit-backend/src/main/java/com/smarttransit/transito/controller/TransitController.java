package com.smarttransit.transito.controller;

import com.smarttransit.transito.dto.RouteDTO;
import com.smarttransit.transito.dto.StopDTO;
import com.smarttransit.transito.service.TransitDataService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/transit")
public class TransitController {

    private final TransitDataService transitDataService;

    public TransitController(TransitDataService transitDataService) {
        this.transitDataService = transitDataService;
    }

    @GetMapping("/routes")
    public ResponseEntity<List<RouteDTO>> getAllRoutes() {
        return ResponseEntity.ok(transitDataService.getAllRoutes());
    }

    @GetMapping("/routes/{routeId}")
    public ResponseEntity<RouteDTO> getRouteById(@PathVariable String routeId) {
        RouteDTO route = transitDataService.getRouteById(routeId);
        if (route != null) {
            return ResponseEntity.ok(route);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/routes/{routeId}/stops")
    public ResponseEntity<List<StopDTO>> getStopsByRoute(@PathVariable String routeId) {
        List<StopDTO> stops = transitDataService.getStopsByRoute(routeId);
        if (!stops.isEmpty()) {
            return ResponseEntity.ok(stops);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/stops")
    public ResponseEntity<List<StopDTO>> getAllStops() {
        return ResponseEntity.ok(transitDataService.getAllStops());
    }
}