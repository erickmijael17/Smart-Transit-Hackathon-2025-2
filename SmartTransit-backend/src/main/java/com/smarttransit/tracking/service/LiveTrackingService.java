package com.smarttransit.tracking.service;

import com.smarttransit.tracking.dto.BusPositionDTO;
import com.smarttransit.transito.dto.RouteDTO;
import com.smarttransit.transito.service.TransitDataService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@EnableScheduling
public class LiveTrackingService {

    private static final Logger log = LoggerFactory.getLogger(LiveTrackingService.class);

    private final SimpMessagingTemplate messagingTemplate;
    private final TransitDataService transitDataService;

    @Value("${tracking.simulator.speed-factor:0.2}")
    private double speedFactor;

    @Value("${tracking.simulator.buses-per-route:2}")
    private int busesPerRoute;

    // Lista que contiene todas las instancias de nuestros buses simulados
    private final List<BusSimulationService> activeBuses = new ArrayList<>();

    public LiveTrackingService(SimpMessagingTemplate messagingTemplate, TransitDataService transitDataService) {
        this.messagingTemplate = messagingTemplate;
        this.transitDataService = transitDataService;
    }

    @PostConstruct
    public void initializeSimulations() {
        List<RouteDTO> routes = transitDataService.getAllRoutes();

        for (RouteDTO route : routes) {
            if (route.getPolylinePointCount() < 2) {
                continue;
            }

            int routeLength = route.getPolylinePointCount();
            int spacing = routeLength / busesPerRoute;

            for (int i = 1; i <= busesPerRoute; i++) {
                String busId = "BUS-" + route.getId() + "-" + String.format("%02d", i);
                int initialPosition = ((i - 1) * spacing) % routeLength;

                // Creamos una nueva instancia del simulador para cada bus
                activeBuses.add(new BusSimulationService(busId, route, initialPosition));
            }
        }

        log.info("✓ Live Tracking Service initialized with {} active bus simulations.", activeBuses.size());
    }

    @Scheduled(fixedRate = 500)
    public void broadcastBusPositions() {
        if (activeBuses.isEmpty()) {
            return;
        }

        for (BusSimulationService bus : activeBuses) {
            // 1. Pedimos al bus que calcule su siguiente posición
            BusPositionDTO newPosition = bus.updatePosition(speedFactor);

            if (newPosition != null) {
                messagingTemplate.convertAndSend("/topic/positions", newPosition);
            }
        }
    }

    /**
     * Publica una posición recibida externamente (por ejemplo, desde la app del conductor) al topic WebSocket.
     */
    public void publishExternalPosition(BusPositionDTO externalPosition) {
        if (externalPosition == null) {
            return;
        }
        messagingTemplate.convertAndSend("/topic/positions", externalPosition);
    }
}