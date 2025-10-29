package com.smarttransit.suggestions.service;

import com.smarttransit.transito.dto.RouteDTO;
import com.smarttransit.transito.dto.StopDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio de scoring inteligente avanzado para sugerencias de rutas
 * Considera múltiples factores y usa lógica avanzada para tomar mejores decisiones
 */
@Service
public class IntelligentScoringService {
    
    private static final Logger log = LoggerFactory.getLogger(IntelligentScoringService.class);
    
    private final GeoUtils geoUtils;
    
    // Pesos ajustados para scoring más inteligente
    private static final double WEIGHT_ACCESS_DISTANCE = 0.30;      // 30% - Distancia de acceso
    private static final double WEIGHT_EGRESS_DISTANCE = 0.30;      // 30% - Distancia de salida
    private static final double WEIGHT_ROUTE_EFFICIENCY = 0.20;     // 20% - Eficiencia de la ruta
    private static final double WEIGHT_ROUTE_COVERAGE = 0.15;       // 15% - Cobertura del recorrido
    private static final double WEIGHT_DIRECTNESS = 0.05;           // 5%  - Qué tan directa es la ruta
    
    public IntelligentScoringService(GeoUtils geoUtils) {
        this.geoUtils = geoUtils;
    }
    
    /**
     * Calcula un score inteligente multi-factor para una ruta
     * 
     * @param route Ruta a evaluar
     * @param userLat Latitud del usuario
     * @param userLon Longitud del usuario
     * @param destLat Latitud del destino
     * @param destLon Longitud del destino
     * @param maxWalkingDistance Distancia máxima de caminata aceptable
     * @return ScoringResult con score y métricas detalladas
     */
    public ScoringResult calculateIntelligentScore(
            RouteDTO route,
            double userLat, double userLon,
            double destLat, double destLon,
            double maxWalkingDistance) {
        
        if (route.getStops() == null || route.getStops().isEmpty()) {
            return null;
        }
        
        // 1. ANÁLISIS DE ACCESO - Encontrar mejor punto de acceso a la ruta
        AccessAnalysis accessAnalysis = analyzeAccess(route, userLat, userLon, maxWalkingDistance);
        if (accessAnalysis == null) {
            return null; // Ruta demasiado lejos para acceder
        }
        
        // 2. ANÁLISIS DE SALIDA - Encontrar mejor punto de salida de la ruta
        EgressAnalysis egressAnalysis = analyzeEgress(route, destLat, destLon, maxWalkingDistance);
        if (egressAnalysis == null) {
            return null; // Ruta no llega cerca del destino
        }
        
        // 3. ANÁLISIS DE EFICIENCIA - Qué tan eficiente es usar esta ruta
        EfficiencyAnalysis efficiencyAnalysis = analyzeEfficiency(
            userLat, userLon,
            accessAnalysis.distance,
            egressAnalysis.distance,
            destLat, destLon
        );
        
        // 4. ANÁLISIS DE COBERTURA - Qué tan bien la ruta cubre el recorrido
        CoverageAnalysis coverageAnalysis = analyzeCoverage(
            route,
            accessAnalysis.stop,
            egressAnalysis.stop
        );
        
        // 5. ANÁLISIS DE DIRECCIONALIDAD - Qué tan directa es la ruta hacia el destino
        DirectnessAnalysis directnessAnalysis = analyzeDirectness(
            userLat, userLon,
            accessAnalysis.stop,
            egressAnalysis.stop,
            destLat, destLon
        );
        
        // 6. CALCULAR SCORE PONDERADO FINAL
        double finalScore = calculateWeightedScore(
            accessAnalysis,
            egressAnalysis,
            efficiencyAnalysis,
            coverageAnalysis,
            directnessAnalysis
        );
        
        log.debug("Route {}: access={:.0f}m, egress={:.0f}m, efficiency={:.1f}, coverage={:.1f}, directness={:.1f} -> SCORE={:.2f}",
                 route.getName(),
                 accessAnalysis.distance,
                 egressAnalysis.distance,
                 efficiencyAnalysis.score,
                 coverageAnalysis.score,
                 directnessAnalysis.score,
                 finalScore);
        
        return ScoringResult.builder()
                .finalScore(finalScore)
                .accessStop(accessAnalysis.stop)
                .accessDistance(accessAnalysis.distance)
                .egressStop(egressAnalysis.stop)
                .egressDistance(egressAnalysis.distance)
                .efficiencyScore(efficiencyAnalysis.score)
                .coverageScore(coverageAnalysis.score)
                .directnessScore(directnessAnalysis.score)
                .build();
    }
    
    /**
     * Analiza el acceso a la ruta desde la ubicación del usuario
     */
    private AccessAnalysis analyzeAccess(RouteDTO route, double userLat, double userLon, double maxDistance) {
        StopDTO nearestStop = null;
        double minDistance = Double.MAX_VALUE;
        
        for (StopDTO stop : route.getStops()) {
            double distance = geoUtils.calculateDistance(userLat, userLon, stop.getLatitude(), stop.getLongitude());
            if (distance < minDistance) {
                minDistance = distance;
                nearestStop = stop;
            }
        }
        
        // Descartar si está muy lejos
        if (minDistance > maxDistance * 2) {
            return null;
        }
        
        // Score: 100 si está muy cerca, 0 si está en el límite
        double score = geoUtils.distanceToScore(minDistance, maxDistance);
        
        return new AccessAnalysis(nearestStop, minDistance, score);
    }
    
    /**
     * Analiza la salida de la ruta hacia el destino
     */
    private EgressAnalysis analyzeEgress(RouteDTO route, double destLat, double destLon, double maxDistance) {
        // Buscar la parada más cercana al destino
        StopDTO nearestStop = null;
        double minDistance = Double.MAX_VALUE;
        
        for (StopDTO stop : route.getStops()) {
            double distance = geoUtils.calculateDistance(destLat, destLon, stop.getLatitude(), stop.getLongitude());
            if (distance < minDistance) {
                minDistance = distance;
                nearestStop = stop;
            }
        }
        
        // También verificar distancia mínima de la polilínea completa
        double polylineDistance = geoUtils.distanceToPolyline(destLat, destLon, route.getPolyline());
        
        // Usar la menor de ambas distancias
        double effectiveDistance = Math.min(minDistance, polylineDistance);
        
        // Score basado en cercanía al destino
        double score = geoUtils.distanceToScore(effectiveDistance, maxDistance);
        
        return new EgressAnalysis(nearestStop, effectiveDistance, score);
    }
    
    /**
     * Analiza la eficiencia total de usar esta ruta
     */
    private EfficiencyAnalysis analyzeEfficiency(
            double userLat, double userLon,
            double accessDistance,
            double egressDistance,
            double destLat, double destLon) {
        
        // Distancia directa en línea recta
        double directDistance = geoUtils.calculateDistance(userLat, userLon, destLat, destLon);
        
        // Distancia total caminando (acceso + salida)
        double totalWalkingDistance = accessDistance + egressDistance;
        
        // Ratio de eficiencia: qué porcentaje del viaje es caminata
        double walkingRatio = directDistance > 0 ? totalWalkingDistance / directDistance : 1.0;
        
        // Score: mejor si la caminata es pequeña comparada con la distancia total
        double score;
        if (walkingRatio <= 0.2) {
            score = 100.0; // Excelente: solo 20% caminata
        } else if (walkingRatio <= 0.4) {
            score = 85.0;  // Muy bueno: 40% caminata
        } else if (walkingRatio <= 0.6) {
            score = 70.0;  // Bueno: 60% caminata
        } else if (walkingRatio <= 0.8) {
            score = 50.0;  // Regular: 80% caminata
        } else {
            score = 30.0;  // No muy eficiente
        }
        
        return new EfficiencyAnalysis(score, walkingRatio, totalWalkingDistance);
    }
    
    /**
     * Analiza qué tan bien la ruta cubre el recorrido necesario
     */
    private CoverageAnalysis analyzeCoverage(RouteDTO route, StopDTO accessStop, StopDTO egressStop) {
        // Calcular cuántas paradas hay entre acceso y salida
        List<StopDTO> allStops = route.getStops();
        int accessIndex = allStops.indexOf(accessStop);
        int egressIndex = allStops.indexOf(egressStop);
        
        if (accessIndex == -1 || egressIndex == -1) {
            return new CoverageAnalysis(50.0, 0);
        }
        
        // Número de paradas cubiertas
        int stopsInRoute = Math.abs(egressIndex - accessIndex);
        
        // Score: mejor si cubre más paradas (más recorrido)
        double score;
        if (stopsInRoute >= 10) {
            score = 100.0; // Recorrido largo
        } else if (stopsInRoute >= 5) {
            score = 85.0;  // Recorrido medio
        } else if (stopsInRoute >= 2) {
            score = 70.0;  // Recorrido corto pero útil
        } else {
            score = 50.0;  // Muy pocas paradas
        }
        
        return new CoverageAnalysis(score, stopsInRoute);
    }
    
    /**
     * Analiza qué tan directa es la ruta (no da muchas vueltas)
     */
    private DirectnessAnalysis analyzeDirectness(
            double userLat, double userLon,
            StopDTO accessStop, StopDTO egressStop,
            double destLat, double destLon) {
        
        // Distancia directa: usuario -> destino
        double directDistance = geoUtils.calculateDistance(userLat, userLon, destLat, destLon);
        
        // Distancia de la ruta: acceso -> salida
        double routeDistance = geoUtils.calculateDistance(
            accessStop.getLatitude(), accessStop.getLongitude(),
            egressStop.getLatitude(), egressStop.getLongitude()
        );
        
        // Ratio de direccionalidad
        double directnessRatio = directDistance > 0 ? routeDistance / directDistance : 1.0;
        
        // Score: mejor si la ruta es más directa
        double score;
        if (directnessRatio <= 1.2) {
            score = 100.0; // Muy directa
        } else if (directnessRatio <= 1.5) {
            score = 80.0;  // Directa
        } else if (directnessRatio <= 2.0) {
            score = 60.0;  // Algunas vueltas
        } else {
            score = 40.0;  // Da vueltas
        }
        
        return new DirectnessAnalysis(score, directnessRatio);
    }
    
    /**
     * Calcula el score final ponderado combinando todos los factores
     */
    private double calculateWeightedScore(
            AccessAnalysis access,
            EgressAnalysis egress,
            EfficiencyAnalysis efficiency,
            CoverageAnalysis coverage,
            DirectnessAnalysis directness) {
        
        double weightedScore = 
            (access.score * WEIGHT_ACCESS_DISTANCE) +
            (egress.score * WEIGHT_EGRESS_DISTANCE) +
            (efficiency.score * WEIGHT_ROUTE_EFFICIENCY) +
            (coverage.score * WEIGHT_ROUTE_COVERAGE) +
            (directness.score * WEIGHT_DIRECTNESS);
        
        return Math.round(weightedScore * 100.0) / 100.0;
    }
    
    // Clases internas para análisis
    private static class AccessAnalysis {
        StopDTO stop;
        double distance;
        double score;
        
        AccessAnalysis(StopDTO stop, double distance, double score) {
            this.stop = stop;
            this.distance = distance;
            this.score = score;
        }
    }
    
    private static class EgressAnalysis {
        StopDTO stop;
        double distance;
        double score;
        
        EgressAnalysis(StopDTO stop, double distance, double score) {
            this.stop = stop;
            this.distance = distance;
            this.score = score;
        }
    }
    
    private static class EfficiencyAnalysis {
        double score;
        double walkingRatio;
        double totalWalkingDistance;
        
        EfficiencyAnalysis(double score, double walkingRatio, double totalWalkingDistance) {
            this.score = score;
            this.walkingRatio = walkingRatio;
            this.totalWalkingDistance = totalWalkingDistance;
        }
    }
    
    private static class CoverageAnalysis {
        double score;
        int stopsInRoute;
        
        CoverageAnalysis(double score, int stopsInRoute) {
            this.score = score;
            this.stopsInRoute = stopsInRoute;
        }
    }
    
    private static class DirectnessAnalysis {
        double score;
        double directnessRatio;
        
        DirectnessAnalysis(double score, double directnessRatio) {
            this.score = score;
            this.directnessRatio = directnessRatio;
        }
    }
    
    /**
     * Resultado del scoring inteligente
     */
    public static class ScoringResult {
        private double finalScore;
        private StopDTO accessStop;
        private double accessDistance;
        private StopDTO egressStop;
        private double egressDistance;
        private double efficiencyScore;
        private double coverageScore;
        private double directnessScore;
        
        public static Builder builder() {
            return new Builder();
        }
        
        public static class Builder {
            private ScoringResult result = new ScoringResult();
            
            public Builder finalScore(double finalScore) {
                result.finalScore = finalScore;
                return this;
            }
            
            public Builder accessStop(StopDTO accessStop) {
                result.accessStop = accessStop;
                return this;
            }
            
            public Builder accessDistance(double accessDistance) {
                result.accessDistance = accessDistance;
                return this;
            }
            
            public Builder egressStop(StopDTO egressStop) {
                result.egressStop = egressStop;
                return this;
            }
            
            public Builder egressDistance(double egressDistance) {
                result.egressDistance = egressDistance;
                return this;
            }
            
            public Builder efficiencyScore(double efficiencyScore) {
                result.efficiencyScore = efficiencyScore;
                return this;
            }
            
            public Builder coverageScore(double coverageScore) {
                result.coverageScore = coverageScore;
                return this;
            }
            
            public Builder directnessScore(double directnessScore) {
                result.directnessScore = directnessScore;
                return this;
            }
            
            public ScoringResult build() {
                return result;
            }
        }
        
        // Getters
        public double getFinalScore() { return finalScore; }
        public StopDTO getAccessStop() { return accessStop; }
        public double getAccessDistance() { return accessDistance; }
        public StopDTO getEgressStop() { return egressStop; }
        public double getEgressDistance() { return egressDistance; }
        public double getEfficiencyScore() { return efficiencyScore; }
        public double getCoverageScore() { return coverageScore; }
        public double getDirectnessScore() { return directnessScore; }
    }
}
