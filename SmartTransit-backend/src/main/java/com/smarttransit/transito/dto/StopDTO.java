package com.smarttransit.transito.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StopDTO {
    private String id;          // ID único de la parada (ej: "18-S001", "22-S001")
    private String name;        // Nombre descriptivo (ej: "Parada 1", "Terminal")
    private double latitude;    // Latitud en grados decimales
    private double longitude;   // Longitud en grados decimales
}