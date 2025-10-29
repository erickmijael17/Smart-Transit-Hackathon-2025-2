package com.smarttransit.route.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarttransit.transito.dto.RouteDTO;
import com.smarttransit.transito.dto.StopDTO;
import com.smarttransit.route.entity.Route;
import com.smarttransit.route.entity.Stop;
import com.smarttransit.route.repository.RouteRepository;
import com.smarttransit.route.repository.StopRepository;
import io.jenetics.jpx.WayPoint;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RouteAdminService {

    private final RouteRepository routeRepository;
    private final StopRepository stopRepository;
    private final GPXParserService gpxParserService;
    private final ObjectMapper objectMapper;

    /**
     * Importa una ruta desde un archivo GPX
     */
    @Transactional
    public RouteDTO importRouteFromGPX(String routeId, String name, String color, 
                                       MultipartFile gpxFile) throws Exception {
        
        // Validar que no exista ya la ruta
        if (routeRepository.existsById(routeId)) {
            throw new IllegalArgumentException("Ya existe una ruta con el ID: " + routeId);
        }

        // Parsear el archivo GPX
        List<List<Double>> polyline = gpxParserService.parseGPXFile(gpxFile);
        
        if (polyline.isEmpty()) {
            throw new IllegalArgumentException("El archivo GPX no contiene puntos válidos");
        }

        // Crear entidad Route
        Route route = new Route();
        route.setId(routeId);
        route.setName(name);
        route.setColor(color);
        route.setPolylineJson(objectMapper.writeValueAsString(polyline));
        route.setActive(true);

        // Guardar ruta
        route = routeRepository.save(route);
        log.info("Ruta {} importada correctamente con {} puntos", routeId, polyline.size());

        // Extraer y guardar paradas si existen
        List<WayPoint> waypoints = gpxParserService.extractStops(gpxFile);
        List<StopDTO> stops = new ArrayList<>();

        for (int i = 0; i < waypoints.size(); i++) {
            WayPoint wp = waypoints.get(i);
            
            Stop stop = new Stop();
            stop.setId(routeId + "_STOP_" + (i + 1));
            stop.setName(wp.getName().orElse("Parada " + (i + 1)));
            stop.setLatitude(wp.getLatitude().doubleValue());
            stop.setLongitude(wp.getLongitude().doubleValue());
            stop.setRouteId(routeId);
            stop.setStopOrder(i);

            stopRepository.save(stop);
            
            stops.add(new StopDTO(
                stop.getId(),
                stop.getName(),
                stop.getLatitude(),
                stop.getLongitude()
            ));
        }

        // Retornar DTO
        return new RouteDTO(route.getId(), route.getName(), route.getColor(), polyline, stops);
    }

    /**
     * Obtiene todas las rutas
     */
    public List<RouteDTO> getAllRoutes() {
        return routeRepository.findByActiveTrue().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Obtiene una ruta por ID
     */
    public RouteDTO getRouteById(String id) {
        Route route = routeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Ruta no encontrada: " + id));
        return convertToDTO(route);
    }

    /**
     * Elimina una ruta
     */
    @Transactional
    public void deleteRoute(String id) {
        stopRepository.deleteByRouteId(id);
        routeRepository.deleteById(id);
        log.info("Ruta {} eliminada correctamente", id);
    }

    /**
     * Convierte entidad Route a DTO
     */
    private RouteDTO convertToDTO(Route route) {
        try {
            List<List<Double>> polyline = objectMapper.readValue(
                route.getPolylineJson(), 
                new TypeReference<List<List<Double>>>() {}
            );

            List<StopDTO> stops = stopRepository.findByRouteIdOrderByStopOrder(route.getId())
                .stream()
                .map(stop -> new StopDTO(
                    stop.getId(),
                    stop.getName(),
                    stop.getLatitude(),
                    stop.getLongitude()
                ))
                .collect(Collectors.toList());

            return new RouteDTO(route.getId(), route.getName(), route.getColor(), polyline, stops);

        } catch (Exception e) {
            log.error("Error al convertir ruta a DTO", e);
            return new RouteDTO(route.getId(), route.getName(), route.getColor(), List.of(), List.of());
        }
    }
}


