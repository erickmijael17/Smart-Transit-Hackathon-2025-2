package com.smarttransit.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarttransit.route.entity.Route;
import com.smarttransit.route.entity.Stop;
import com.smarttransit.route.repository.RouteRepository;
import com.smarttransit.route.repository.StopRepository;
import io.jenetics.jpx.GPX;
import io.jenetics.jpx.TrackSegment;
import io.jenetics.jpx.WayPoint;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


@Configuration
@RequiredArgsConstructor
@Slf4j
public class RouteDataInitializer {

    private final RouteRepository routeRepository;
    private final StopRepository stopRepository;
    private final ObjectMapper objectMapper;

    @Bean
    @Order(1) // Ejecutar primero, antes que otros inicializadores
    CommandLineRunner loadGPXRoutesToDatabase() {
        return args -> {
            try {
                log.info("Iniciando carga de rutas GPX a base de datos");

                
                // Resolver archivos GPX
                PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
                Resource[] gpxFiles = resolver.getResources("classpath:data/*.gpx");
                
                log.info("Encontrados {} archivos GPX", gpxFiles.length);
                
                int loaded = 0;
                int skipped = 0;
                int errors = 0;
                
                // Mapa de colores por ruta
                Map<String, String> routeColors = getRouteColorMap();
                
                for (Resource gpxFile : gpxFiles) {
                    try {
                        String filename = gpxFile.getFilename();
                        String routeId = extractRouteIdentifier(filename);
                        
                        // Verificar si ya existe
                        if (routeRepository.existsById(routeId)) {
                            log.info("Ruta {} ya existe en BD, omitiendo...", routeId);
                            skipped++;
                            continue;
                        }
                        
                        log.info("Procesando: {} (ID: {})", filename, routeId);
                        
                        // Parsear GPX
                        GPXData gpxData = parseGPXFile(gpxFile);
                        
                        if (gpxData.polyline.isEmpty()) {
                            log.warn("Archivo {} no contiene datos de ruta válidos", filename);
                            errors++;
                            continue;
                        }
                        
                        // Crear entidad Route
                        Route route = new Route();
                        route.setId(routeId);
                        route.setName("Línea " + routeId.toUpperCase());
                        route.setColor(routeColors.getOrDefault(routeId, "#fd7e14"));
                        route.setPolylineJson(objectMapper.writeValueAsString(gpxData.polyline));
                        route.setActive(true);
                        
                        // Guardar ruta
                        routeRepository.save(route);
                        
                        // Guardar paradas
                        int stopOrder = 0;
                        for (WayPoint wp : gpxData.waypoints) {
                            Stop stop = new Stop();
                            stop.setId(routeId + "_STOP_" + (stopOrder + 1));
                            stop.setName(wp.getName().orElse("Parada " + (stopOrder + 1)));
                            stop.setLatitude(wp.getLatitude().doubleValue());
                            stop.setLongitude(wp.getLongitude().doubleValue());
                            stop.setRouteId(routeId);
                            stop.setStopOrder(stopOrder++);
                            stopRepository.save(stop);
                        }
                        
                        log.info("Ruta {} cargada: {} puntos, {} paradas",
                                routeId, gpxData.polyline.size(), gpxData.waypoints.size());
                        loaded++;
                        
                    } catch (Exception e) {
                        log.error(" Error procesando {}: {}", gpxFile.getFilename(), e.getMessage());
                        errors++;
                    }
                }
                
                log.info("Carga completada:");
                log.info("     • Cargadas: {} rutas", loaded);
                log.info("     • Omitidas: {} rutas (ya existían)", skipped);
                log.info("     • Errores:  {} rutas", errors);

                // Mostrar resumen de rutas en BD
                long totalRoutes = routeRepository.count();
                long totalStops = stopRepository.count();
                log.info("Total en base de datos:");
                log.info("     • {} rutas activas", totalRoutes);
                log.info("     • {} paradas", totalStops);
                
            } catch (Exception e) {
                log.error("Error crítico al cargar rutas GPX", e);
            }
        };
    }
    
    /**
     * Parsea un archivo GPX y extrae polyline y waypoints
     */
    private GPXData parseGPXFile(Resource gpxFile) throws IOException {
        // Crear archivo temporal
        Path tempFile = Files.createTempFile("gpx-", ".gpx");
        try {
            // Copiar contenido
            Files.copy(gpxFile.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);
            
            // Leer GPX
            GPX gpx = GPX.read(tempFile);
            
            List<List<Double>> polyline = new ArrayList<>();
            
            // Extraer puntos de tracks
            for (io.jenetics.jpx.Track track : gpx.getTracks()) {
                for (TrackSegment segment : track.getSegments()) {
                    List<List<Double>> points = segment.getPoints().stream()
                        .map(wp -> Arrays.asList(
                            wp.getLatitude().doubleValue(),
                            wp.getLongitude().doubleValue()
                        ))
                        .collect(Collectors.toList());
                    polyline.addAll(points);
                }
            }
            
            // Si no hay tracks, usar routes
            if (polyline.isEmpty()) {
                for (io.jenetics.jpx.Route route : gpx.getRoutes()) {
                    List<List<Double>> points = route.getPoints().stream()
                        .map(wp -> Arrays.asList(
                            wp.getLatitude().doubleValue(),
                            wp.getLongitude().doubleValue()
                        ))
                        .collect(Collectors.toList());
                    polyline.addAll(points);
                }
            }
            
            // Si aún no hay datos, usar waypoints
            if (polyline.isEmpty()) {
                List<List<Double>> points = gpx.getWayPoints().stream()
                    .map(wp -> Arrays.asList(
                        wp.getLatitude().doubleValue(),
                        wp.getLongitude().doubleValue()
                    ))
                    .collect(Collectors.toList());
                polyline.addAll(points);
            }
            
            // Extraer waypoints (paradas)
            List<WayPoint> waypoints = gpx.getWayPoints().stream()
                .filter(wp -> wp.getName().isPresent())
                .collect(Collectors.toList());
            
            return new GPXData(polyline, waypoints);
            
        } finally {
            // Limpiar archivo temporal
            try {
                Files.deleteIfExists(tempFile);
            } catch (IOException ignored) {}
        }
    }
    
    /**
     * Extrae el identificador de ruta del nombre de archivo
     */
    private String extractRouteIdentifier(String filename) {
        if (filename == null) return "UNKNOWN";
        
        String nameWithoutExtension = filename.replaceAll("\\.gpx$", "");
        String[] parts = nameWithoutExtension.split("-");
        
        if (parts.length > 0) {
            String identifier = parts[parts.length - 1].trim();
            return identifier.toUpperCase();
        }
        
        return "UNKNOWN";
    }
    
    /**
     * Mapa de colores específicos por línea
     */
    private Map<String, String> getRouteColorMap() {
        Map<String, String> colors = new HashMap<>();
        colors.put("1", "#FF6B6B");    // Rojo
        colors.put("10", "#4ECDC4");   // Turquesa
        colors.put("11", "#45B7D1");   // Azul claro
        colors.put("12", "#96CEB4");   // Verde menta
        colors.put("15", "#FFEAA7");   // Amarillo
        colors.put("17", "#DFE6E9");   // Gris claro
        colors.put("18", "#007bff");   // Azul
        colors.put("19", "#74B9FF");   // Azul cielo
        colors.put("21", "#FD79A8");   // Rosa
        colors.put("22", "#28a745");   // Verde
        colors.put("23", "#FDCB6E");   // Naranja claro
        colors.put("B", "#dc3545");    // Rojo oscuro
        colors.put("A", "#ffc107");    // Amarillo dorado
        colors.put("C", "#17a2b8");    // Cyan
        colors.put("D", "#6f42c1");    // Púrpura
        return colors;
    }
    
    /**
     * Clase auxiliar para datos del GPX
     */
    private static class GPXData {
        final List<List<Double>> polyline;
        final List<WayPoint> waypoints;
        
        GPXData(List<List<Double>> polyline, List<WayPoint> waypoints) {
            this.polyline = polyline;
            this.waypoints = waypoints;
        }
    }
}

