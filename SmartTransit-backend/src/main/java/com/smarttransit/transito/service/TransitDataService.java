package com.smarttransit.transito.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarttransit.route.repository.RouteRepository;
import com.smarttransit.route.repository.StopRepository;
import com.smarttransit.transito.dto.RouteDTO;
import com.smarttransit.transito.dto.StopDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de datos de tránsito que consulta rutas y paradas desde la base de datos.
 * Las rutas GPX se cargan automáticamente a la BD mediante RouteDataInitializer al iniciar la app.
 * 
 * Endpoints servidos:
 * - GET /api/transit/routes
 * - GET /api/transit/routes/{id}
 * - GET /api/transit/routes/{id}/stops
 * - GET /api/transit/stops
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TransitDataService {

    private final RouteRepository routeRepository;
    private final StopRepository stopRepository;
    private final ObjectMapper objectMapper;

    /**
     * Obtiene todas las rutas activas desde la base de datos
     */
    public List<RouteDTO> getAllRoutes() {
        try {
            return routeRepository.findByActiveTrue().stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error al obtener rutas desde la BD", e);
            return List.of();
        }
    }

    /**
     * Obtiene todas las paradas de todas las rutas
     */
    public List<StopDTO> getAllStops() {
        try {
            return stopRepository.findAll().stream()
                    .map(stop -> new StopDTO(
                            stop.getId(),
                            stop.getName(),
                            stop.getLatitude(),
                            stop.getLongitude()
                    ))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error al obtener paradas desde la BD", e);
            return List.of();
        }
    }

    /**
     * Obtiene una ruta específica por su ID
     */
    public RouteDTO getRouteById(String routeId) {
        try {
            return routeRepository.findById(routeId)
                    .map(this::convertToDTO)
                    .orElse(null);
        } catch (Exception e) {
            log.error("Error al obtener ruta {} desde la BD", routeId, e);
            return null;
        }
    }

    /**
     * Obtiene las paradas de una ruta específica
     */
    public List<StopDTO> getStopsByRoute(String routeId) {
        try {
            return stopRepository.findByRouteIdOrderByStopOrder(routeId).stream()
                    .map(stop -> new StopDTO(
                            stop.getId(),
                            stop.getName(),
                            stop.getLatitude(),
                            stop.getLongitude()
                    ))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error al obtener paradas de ruta {} desde la BD", routeId, e);
            return List.of();
        }
    }

    /**
     * Convierte una entidad Route de BD a RouteDTO
     */
    private RouteDTO convertToDTO(com.smarttransit.route.entity.Route route) {
        try {
            // Parsear polyline JSON
            List<List<Double>> polyline = objectMapper.readValue(
                    route.getPolylineJson(),
                    new TypeReference<List<List<Double>>>() {}
            );

            // Obtener stops
            List<StopDTO> stops = stopRepository.findByRouteIdOrderByStopOrder(route.getId())
                    .stream()
                    .map(stop -> new StopDTO(
                            stop.getId(),
                            stop.getName(),
                            stop.getLatitude(),
                            stop.getLongitude()
                    ))
                    .collect(Collectors.toList());

            return new RouteDTO(
                    route.getId(),
                    route.getName(),
                    route.getColor(),
                    polyline,
                    stops
            );
        } catch (Exception e) {
            log.error("Error al convertir ruta {} a DTO", route.getId(), e);
            // Retornar DTO básico sin polyline ni stops
            return new RouteDTO(
                    route.getId(),
                    route.getName(),
                    route.getColor(),
                    List.of(),
                    List.of()
            );
        }
    }
}
