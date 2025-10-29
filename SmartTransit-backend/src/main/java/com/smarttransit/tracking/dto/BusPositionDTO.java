package com.smarttransit.tracking.dto;

import com.smarttransit.tracking.model.BusStatus;
import com.smarttransit.tracking.model.OccupancyLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BusPositionDTO {
    // Campos requeridos
    private String busId;           // ID único del bus (ej: "BUS_123")
    private String routeId;         // ID de la ruta (ej: "18")
    private String routeName;       // Nombre de la ruta (ej: "Línea 18")
    private double latitude;        // Latitud actual
    private double longitude;       // Longitud actual
    private double bearing;         // Dirección del movimiento en grados (0-360)
    private double speed;           // Velocidad en km/h
    private BusStatus status;       // Estado: ACTIVE, DELAYED, STOPPED, etc.
    private LocalDateTime timestamp; // Timestamp de la actualización
    private int progress;           // Progreso en la ruta (0-100%)
    
    // Campos opcionales
    private OccupancyLevel occupancyLevel; // Nivel de ocupación (EMPTY, AVAILABLE, CROWDED, FULL)
    private String nextStopId;      // ID de la próxima parada
    private String nextStopName;    // Nombre de la próxima parada
    private LocalDateTime estimatedArrivalTime; // Tiempo estimado de llegada
    private Integer distanceToDestination; // Metros hasta el destino
    private Integer passengerCount; // Número de pasajeros
    private Integer delay;          // Minutos de retraso (negativo = adelantado)
}