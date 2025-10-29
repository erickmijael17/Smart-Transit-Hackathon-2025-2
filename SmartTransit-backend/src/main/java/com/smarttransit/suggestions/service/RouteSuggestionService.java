package com.smarttransit.suggestions.service;

import com.smarttransit.mapa.dto.GeocodingResponse;
import com.smarttransit.search.service.PhotonService;
import com.smarttransit.suggestions.dto.RouteSuggestionRequest;
import com.smarttransit.suggestions.dto.RouteSuggestionResponse;
import com.smarttransit.suggestions.dto.SuggestionResult;
import com.smarttransit.suggestions.dto.WalkingPath;
import com.smarttransit.transito.dto.RouteDTO;
import com.smarttransit.transito.dto.StopDTO;
import com.smarttransit.transito.service.TransitDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio principal para generar sugerencias de rutas de transporte público
 * usando algoritmos de scoring basados en distancias geográficas
 */
@Service
public class RouteSuggestionService {
    
    private static final Logger log = LoggerFactory.getLogger(RouteSuggestionService.class);
    
    private final TransitDataService transitDataService;
    private final PhotonService photonService;
    private final GeoUtils geoUtils;
    private final WalkingRouteService walkingRouteService;
    private final IntelligentScoringService intelligentScoringService;
    
    // Parámetros de configuración para el scoring
    private static final double MAX_WALKING_DISTANCE = 1000.0; // 1km máximo de caminata
    private static final double WEIGHT_USER_PROXIMITY = 0.4;   // 40% peso a cercanía del usuario
    private static final double WEIGHT_DEST_PROXIMITY = 0.4;   // 40% peso a cercanía al destino
    private static final double WEIGHT_ROUTE_COVERAGE = 0.2;   // 20% peso a cobertura total
    
    public RouteSuggestionService(TransitDataService transitDataService, 
                                   PhotonService photonService,
                                   GeoUtils geoUtils,
                                   WalkingRouteService walkingRouteService,
                                   IntelligentScoringService intelligentScoringService) {
        this.transitDataService = transitDataService;
        this.photonService = photonService;
        this.geoUtils = geoUtils;
        this.walkingRouteService = walkingRouteService;
        this.intelligentScoringService = intelligentScoringService;
    }
    
    /**
     * Genera sugerencias de rutas basadas en la ubicación actual y el destino
     */
    public SuggestionResult getSuggestions(RouteSuggestionRequest request) throws IOException {
        log.info("Generating route suggestions from ({}, {})", 
                 request.getCurrentLatitude(), request.getCurrentLongitude());
        
        // Paso 1: Validar ubicación actual
        if (request.getCurrentLatitude() == null || request.getCurrentLongitude() == null) {
            throw new IllegalArgumentException("Current location is required");
        }
        
        // Paso 2: Obtener coordenadas del destino (directas o geocodificadas)
        GeocodingResponse destination;
        
        if (request.getDestinationLatitude() != null && request.getDestinationLongitude() != null) {
            // Usar coordenadas directas del destino
            log.info("Using direct destination coordinates: ({}, {})", 
                     request.getDestinationLatitude(), request.getDestinationLongitude());
            
            destination = GeocodingResponse.builder()
                .latitude(request.getDestinationLatitude())
                .longitude(request.getDestinationLongitude())
                .displayName(request.getDestinationName() != null ? 
                            request.getDestinationName() : "Destino seleccionado")
                .build();
                
        } else if (request.getDestination() != null && !request.getDestination().isEmpty()) {
            // Geocodificar el destino usando PhotonService
            // IMPORTANTE: Agregar "Juliaca" para limitar búsqueda a la ciudad
            String enhancedQuery = request.getDestination() + ", Juliaca";
            log.info("Geocoding destination text: '{}' (enhanced: '{}')", 
                     request.getDestination(), enhancedQuery);
            
            List<GeocodingResponse> destinationResults = photonService.search(
                enhancedQuery, 
                request.getCurrentLatitude(), 
                request.getCurrentLongitude(), 
                1
            );
            
            if (destinationResults.isEmpty()) {
                log.warn("No destination found for query: {}", request.getDestination());
                return SuggestionResult.builder()
                    .currentLocation(buildLocationInfo(
                        request.getCurrentLatitude(), 
                        request.getCurrentLongitude(), 
                        "Tu ubicación actual"
                    ))
                    .suggestions(new ArrayList<>())
                    .message("No se encontró el destino especificado")
                    .build();
            }
            
            destination = destinationResults.get(0);
            
        } else {
            throw new IllegalArgumentException("Destination is required (either coordinates or text)");
        }
        log.info("Destination found: {} at ({}, {})", 
                 destination.getDisplayName(), destination.getLatitude(), destination.getLongitude());
        
        // Paso 3: Obtener todas las rutas disponibles
        List<RouteDTO> allRoutes = transitDataService.getAllRoutes();
        log.info("Analyzing {} routes", allRoutes.size());
        
        // Paso 4: Calcular scoring para cada ruta
        List<RouteSuggestionResponse> scoredRoutes = new ArrayList<>();
        
        // Determinar distancia máxima de caminata (usar la del request o default)
        double maxWalkingDistance = request.getMaxWalkingDistance() != null 
            ? request.getMaxWalkingDistance() 
            : MAX_WALKING_DISTANCE;
        
        log.info("Using max walking distance: {}m", maxWalkingDistance);
        
        for (RouteDTO route : allRoutes) {
            RouteSuggestionResponse suggestion = scoreRoute(
                route,
                request.getCurrentLatitude(),
                request.getCurrentLongitude(),
                destination.getLatitude(),
                destination.getLongitude(),
                maxWalkingDistance
            );
            
            if (suggestion != null) {
                scoredRoutes.add(suggestion);
            }
        }
        
        // Paso 5: Ordenar por score y tomar las mejores sugerencias
        List<RouteSuggestionResponse> topSuggestions = scoredRoutes.stream()
            .sorted(Comparator.comparingDouble(RouteSuggestionResponse::getScore).reversed())
            .limit(request.getMaxSuggestions() != null ? request.getMaxSuggestions() : 3)
            .collect(Collectors.toList());
        
        log.info("Top {} suggestions generated", topSuggestions.size());
        
        // Paso 6: Construir resultado
        String message = topSuggestions.isEmpty() 
            ? "No se encontraron rutas cercanas a tu ubicación y destino"
            : String.format("Se encontraron %d rutas sugeridas", topSuggestions.size());
        
        return SuggestionResult.builder()
            .currentLocation(buildLocationInfo(
                request.getCurrentLatitude(), 
                request.getCurrentLongitude(), 
                "Tu ubicación actual"
            ))
            .destination(buildLocationInfo(
                destination.getLatitude(), 
                destination.getLongitude(), 
                destination.getDisplayName()
            ))
            .suggestions(topSuggestions)
            .message(message)
            .build();
    }
    
    /**
     * Calcula el score de una ruta usando ALGORITMO INTELIGENTE AVANZADO
     * Considera múltiples factores: acceso, salida, eficiencia, cobertura, direccionalidad
     */
    private RouteSuggestionResponse scoreRoute(RouteDTO route, 
                                                 double userLat, double userLon,
                                                 double destLat, double destLon,
                                                 double maxWalkingDistance) {
        
        // Usar el servicio de scoring inteligente
        IntelligentScoringService.ScoringResult scoringResult = 
            intelligentScoringService.calculateIntelligentScore(
                route, userLat, userLon, destLat, destLon, maxWalkingDistance
            );
        
        // Si el scoring retorna null, significa que la ruta no es viable
        if (scoringResult == null) {
            return null;
        }
        
        // Extraer resultados del scoring
        StopDTO nearestStopToUser = scoringResult.getAccessStop();
        StopDTO nearestStopToDestination = scoringResult.getEgressStop();
        double accessDistance = scoringResult.getAccessDistance();
        double egressDistance = scoringResult.getEgressDistance();
        double finalScore = scoringResult.getFinalScore();
        
        // Generar razón mejorada con información de scoring inteligente
        String reason = generateIntelligentReason(scoringResult);
        
        // Generar trazados de caminata REALES que siguen calles
        List<WalkingPath> walkingPaths = generateWalkingPaths(
            userLat, userLon,
            nearestStopToUser,
            nearestStopToDestination,
            destLat, destLon,
            accessDistance,
            egressDistance
        );
        
        return RouteSuggestionResponse.builder()
            .routeId(route.getId())
            .routeName(route.getName())
            .routeColor(route.getColor())
            .score(finalScore)
            .nearestStopToUser(nearestStopToUser)
            .distanceToNearestStop(Math.round(accessDistance * 10.0) / 10.0)
            .nearestStopToDestination(nearestStopToDestination)
            .distanceFromRouteToDestination(Math.round(egressDistance * 10.0) / 10.0)
            .reason(reason)
            .walkingPaths(walkingPaths)
            .build();
    }
    
    /**
     * Genera una razón inteligente basada en el scoring avanzado
     */
    private String generateIntelligentReason(IntelligentScoringService.ScoringResult scoringResult) {
        StringBuilder reason = new StringBuilder();
        
        double accessDist = scoringResult.getAccessDistance();
        double egressDist = scoringResult.getEgressDistance();
        double efficiencyScore = scoringResult.getEfficiencyScore();
        double finalScore = scoringResult.getFinalScore();
        
        // Análisis de acceso
        if (accessDist < 200) {
            reason.append("Muy cerca de tu ubicación");
        } else if (accessDist < 500) {
            reason.append("Cerca de tu ubicación");
        } else {
            reason.append("A distancia caminable");
        }
        
        reason.append(" y ");
        
        // Análisis de salida
        if (egressDist < 200) {
            reason.append("te deja muy cerca del destino");
        } else if (egressDist < 500) {
            reason.append("te deja cerca del destino");
        } else {
            reason.append("pasa cerca del destino");
        }
        
        // Análisis de eficiencia
        if (efficiencyScore >= 85) {
            reason.append(". Ruta muy eficiente");
        } else if (efficiencyScore >= 70) {
            reason.append(". Ruta eficiente");
        }
        
        // Score final
        if (finalScore >= 85) {
            reason.append(". ¡Excelente opción!");
        } else if (finalScore >= 70) {
            reason.append(". Muy buena opción.");
        } else if (finalScore >= 60) {
            reason.append(". Buena opción.");
        }
        
        return reason.toString();
    }
    
    /**
     * Calcula el score de cobertura: qué tan bien la ruta conecta origen y destino
     * @deprecated Usar IntelligentScoringService en su lugar
     */
    @Deprecated
    private double calculateCoverageScore(double userDistance, double destDistance, double totalDistance) {
        // Si la suma de distancias de acceso es menor que la distancia total directa,
        // significa que la ruta hace una buena conexión
        double accessDistance = userDistance + destDistance;
        
        if (totalDistance == 0) {
            return 100.0;
        }
        
        // Ratio de eficiencia: menor es mejor
        double efficiencyRatio = accessDistance / totalDistance;
        
        // Convertir a score: valores menores a 0.5 son excelentes
        if (efficiencyRatio <= 0.3) {
            return 100.0;
        } else if (efficiencyRatio <= 0.6) {
            return 80.0;
        } else if (efficiencyRatio <= 1.0) {
            return 60.0;
        } else if (efficiencyRatio <= 1.5) {
            return 40.0;
        } else {
            return 20.0;
        }
    }
    
    /**
     * Genera una descripción textual de por qué se sugiere esta ruta
     * @deprecated Usar generateIntelligentReason en su lugar
     */
    @Deprecated
    private String generateReason(double userDistance, double destDistance, double score) {
        StringBuilder reason = new StringBuilder();
        
        if (userDistance < 300) {
            reason.append("Muy cerca de tu ubicación");
        } else if (userDistance < 600) {
            reason.append("Cerca de tu ubicación");
        } else {
            reason.append("A distancia caminable");
        }
        
        reason.append(" y ");
        
        if (destDistance < 300) {
            reason.append("te deja muy cerca del destino");
        } else if (destDistance < 600) {
            reason.append("te deja cerca del destino");
        } else {
            reason.append("pasa cerca del destino");
        }
        
        if (score >= 80) {
            reason.append(". ¡Excelente opción!");
        } else if (score >= 60) {
            reason.append(". Buena opción.");
        }
        
        return reason.toString();
    }
    
    /**
     * Genera trazados de caminata visuales para guiar al usuario
     * Usa OSRM para obtener rutas REALES que siguen las calles (no líneas rectas)
     * Crea dos trazados:
     * 1. Desde ubicación actual → parada más cercana
     * 2. Desde parada final → destino
     */
    private List<WalkingPath> generateWalkingPaths(
            double userLat, double userLon,
            StopDTO stopNearUser,
            StopDTO stopNearDest,
            double destLat, double destLon,
            double distanceToStop,
            double distanceFromStop) {
        
        List<WalkingPath> paths = new ArrayList<>();
        
        // Trazado 1: Usuario → Parada más cercana (RUTA REAL)
        if (stopNearUser != null) {
            WalkingRouteService.WalkingRouteResult route = walkingRouteService.getWalkingRoute(
                userLat, userLon,
                stopNearUser.getLatitude(), stopNearUser.getLongitude()
            );
            
            String description = route.isFollowsRoads()
                ? String.format("Camina %.0fm por las calles hasta %s", route.getDistance(), stopNearUser.getName())
                : String.format("Camina %.0fm hasta %s", route.getDistance(), stopNearUser.getName());
            
            WalkingPath userToStop = WalkingPath.builder()
                .type(WalkingPath.PathType.USER_TO_STOP)
                .coordinates(route.getCoordinates())
                .distance(Math.round(route.getDistance() * 10.0) / 10.0)
                .estimatedWalkingTime(route.getDuration())
                .description(description)
                .color("#4A90E2") // Azul para trazado inicial
                .followsRoads(route.isFollowsRoads())
                .build();
            
            paths.add(userToStop);
            
            log.debug("User to stop route: {}m, {} min, follows roads: {}", 
                     route.getDistance(), route.getDuration(), route.isFollowsRoads());
        }
        
        // Trazado 2: Parada final → Destino (RUTA REAL)
        if (stopNearDest != null) {
            WalkingRouteService.WalkingRouteResult route = walkingRouteService.getWalkingRoute(
                stopNearDest.getLatitude(), stopNearDest.getLongitude(),
                destLat, destLon
            );
            
            String description = route.isFollowsRoads()
                ? String.format("Camina %.0fm por las calles desde %s hasta tu destino", 
                               route.getDistance(), stopNearDest.getName())
                : String.format("Camina %.0fm desde %s hasta tu destino", 
                               route.getDistance(), stopNearDest.getName());
            
            WalkingPath stopToDest = WalkingPath.builder()
                .type(WalkingPath.PathType.STOP_TO_DESTINATION)
                .coordinates(route.getCoordinates())
                .distance(Math.round(route.getDistance() * 10.0) / 10.0)
                .estimatedWalkingTime(route.getDuration())
                .description(description)
                .color("#50C878") // Verde para trazado final
                .followsRoads(route.isFollowsRoads())
                .build();
            
            paths.add(stopToDest);
            
            log.debug("Stop to dest route: {}m, {} min, follows roads: {}", 
                     route.getDistance(), route.getDuration(), route.isFollowsRoads());
        }
        
        log.info("Generated {} walking paths with REAL street routing", paths.size());
        return paths;
    }
    
    /**
     * Helper para construir LocationInfo
     */
    private SuggestionResult.LocationInfo buildLocationInfo(Double lat, Double lon, String name) {
        return SuggestionResult.LocationInfo.builder()
            .latitude(lat)
            .longitude(lon)
            .displayName(name)
            .build();
    }
}
