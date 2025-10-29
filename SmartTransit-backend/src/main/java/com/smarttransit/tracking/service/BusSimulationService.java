package com.smarttransit.tracking.service;

import com.smarttransit.tracking.dto.BusPositionDTO;
import com.smarttransit.tracking.model.BusStatus;
import com.smarttransit.tracking.model.OccupancyLevel;
import com.smarttransit.transito.dto.RouteDTO;

import java.time.LocalDateTime;
import java.util.List;

public class BusSimulationService {

    // 1. Enum para representar la dirección del bus
    private enum Direction {
        FORWARD, BACKWARD
    }

    private final String busId;
    private final RouteDTO route;
    private int currentPointIndex;
    private double progressOnSegment;
    private Direction direction = Direction.FORWARD;

    public BusSimulationService(String busId, RouteDTO route, int initialPosition) {
        this.busId = busId;
        this.route = route;
        this.currentPointIndex = initialPosition;
        this.progressOnSegment = 0.0;
    }

    /**
     * Avanza la simulación del bus un "tick" y devuelve su nueva posición.
     * @param speedFactor Cuánto debe avanzar en este tick.
     * @return Un DTO con la nueva posición del bus.
     */
    public BusPositionDTO updatePosition(double speedFactor) {
        List<List<Double>> polyline = route.getPolyline();
        if (polyline.size() < 2) {
            return null;
        }

        this.progressOnSegment += speedFactor;

        if (this.progressOnSegment >= 1.0) {
            this.progressOnSegment -= 1.0;

            if (direction == Direction.FORWARD) {
                this.currentPointIndex++;
            } else {
                this.currentPointIndex--;
            }


            if (direction == Direction.FORWARD && currentPointIndex >= polyline.size() - 1) {
                this.currentPointIndex = polyline.size() - 1;
                this.direction = Direction.BACKWARD;
            }

            else if (direction == Direction.BACKWARD && currentPointIndex < 0) {
                this.currentPointIndex = 0;
                this.direction = Direction.FORWARD;
            }
        }

        int startIndex = this.currentPointIndex;
        int endIndex = (direction == Direction.FORWARD)
                ? Math.min(startIndex + 1, polyline.size() - 1)
                : Math.max(startIndex - 1, 0);

        List<Double> startPoint = polyline.get(startIndex);
        List<Double> endPoint = polyline.get(endIndex);

        double lat = lerp(startPoint.get(0), endPoint.get(0), this.progressOnSegment);
        double lon = lerp(startPoint.get(1), endPoint.get(1), this.progressOnSegment);

        double bearing = (direction == Direction.FORWARD)
                ? calculateBearing(startPoint.get(0), startPoint.get(1), endPoint.get(0), endPoint.get(1))
                : calculateBearing(endPoint.get(0), endPoint.get(1), startPoint.get(0), startPoint.get(1));

        int progress = (int) (((startIndex + this.progressOnSegment) * 100.0) / polyline.size());
        double speed = 35.0 + (Math.random() * 15);

        BusPositionDTO dto = new BusPositionDTO();
        dto.setBusId(this.busId);
        dto.setRouteId(this.route.getId());
        dto.setRouteName(this.route.getName());
        dto.setLatitude(lat);
        dto.setLongitude(lon);
        dto.setBearing(bearing);
        dto.setSpeed(speed);
        dto.setStatus(BusStatus.ACTIVE);
        dto.setTimestamp(LocalDateTime.now());
        dto.setProgress(progress);
        dto.setOccupancyLevel(OccupancyLevel.AVAILABLE);
        
        return dto;
    }

    /**
     * Interpolación lineal entre dos valores.
     */
    private double lerp(double start, double end, double progress) {
        return start + progress * (end - start);
    }

    /**
     * Calcula el rumbo (bearing) en grados entre dos puntos geográficos.
     */
    private double calculateBearing(double lat1, double lon1, double lat2, double lon2) {
        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double lat2Rad = Math.toRadians(lat2);
        double lon2Rad = Math.toRadians(lon2);

        double dLon = lon2Rad - lon1Rad;

        double y = Math.sin(dLon) * Math.cos(lat2Rad);
        double x = Math.cos(lat1Rad) * Math.sin(lat2Rad) - Math.sin(lat1Rad) * Math.cos(lat2Rad) * Math.cos(dLon);

        double bearingRad = Math.atan2(y, x);

        return (Math.toDegrees(bearingRad) + 360) % 360;
    }
}