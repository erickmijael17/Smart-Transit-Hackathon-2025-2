package com.smarttransit.transito.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouteDTO {
    private String id;              // ID de la ruta (ej: "18", "22", "B")
    private String name;            // Nombre descriptivo (ej: "Línea 18")
    private String color;           // Color hexadecimal para visualización (ej: "#007bff")
    private List<List<Double>> polyline;  // Coordenadas del recorrido [[lat, lon], ...]
    private List<StopDTO> stops;    // Paradas de esta ruta en orden
    
    /**
     * Obtiene el número de paradas en esta ruta
     */
    public int getStopCount() {
        return stops != null ? stops.size() : 0;
    }
    
    /**
     * Obtiene el número de puntos en la polilínea
     */
    public int getPolylinePointCount() {
        return polyline != null ? polyline.size() : 0;
    }
}