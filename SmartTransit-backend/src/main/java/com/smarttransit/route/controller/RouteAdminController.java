package com.smarttransit.route.controller;

import com.smarttransit.transito.dto.RouteDTO;
import com.smarttransit.route.service.RouteAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/routes")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:4200")
public class RouteAdminController {

    private final RouteAdminService routeAdminService;

    /**
     * Importa una ruta desde un archivo GPX
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadRoute(
            @RequestParam("file") MultipartFile file,
            @RequestParam("name") String name,
            @RequestParam("color") String color) {
        
        try {
            // Validar archivo
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "El archivo está vacío"));
            }

            if (!file.getOriginalFilename().toLowerCase().endsWith(".gpx")) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Solo se permiten archivos GPX"));
            }

            // Generar ID de ruta
            String routeId = generateRouteId(name);

            // Importar ruta
            RouteDTO routeDTO = routeAdminService.importRouteFromGPX(
                routeId, name, color, file
            );

            log.info("Ruta {} importada exitosamente", routeId);

            return ResponseEntity.ok(Map.of(
                "message", "Ruta importada correctamente",
                "route", routeDTO
            ));

        } catch (IllegalArgumentException e) {
            log.error("Error de validación al importar ruta", e);
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));

        } catch (Exception e) {
            log.error("Error al importar ruta", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error al procesar archivo GPX: " + e.getMessage()));
        }
    }

    /**
     * Obtiene todas las rutas
     */
    @GetMapping
    public ResponseEntity<List<RouteDTO>> getAllRoutes() {
        List<RouteDTO> routes = routeAdminService.getAllRoutes();
        return ResponseEntity.ok(routes);
    }

    /**
     * Obtiene una ruta por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<RouteDTO> getRouteById(@PathVariable String id) {
        try {
            RouteDTO route = routeAdminService.getRouteById(id);
            return ResponseEntity.ok(route);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Elimina una ruta
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRoute(@PathVariable String id) {
        try {
            routeAdminService.deleteRoute(id);
            return ResponseEntity.ok(Map.of("message", "Ruta eliminada correctamente"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Genera un ID de ruta único
     */
    private String generateRouteId(String name) {
        // Extrae número si existe en el nombre
        String number = name.replaceAll("[^0-9]", "");
        if (!number.isEmpty()) {
            return number;
        }
        // Si no hay número, usar timestamp
        return String.valueOf(System.currentTimeMillis() % 10000);
    }
}


