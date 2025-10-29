package com.smarttransit.pois.dto;

import com.smarttransit.pois.model.PoiType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class PoiSearchRequest {
    @NotNull(message = "Latitud es requerida")
    @Min(value = -90, message = "Latitud debe ser >= -90")
    @Max(value = 90, message = "Latitud debe ser <= 90")
    private Double latitude;

    @NotNull(message = "Longitud es requerida")
    @Min(value = -180, message = "Longitud debe ser >= -180")
    @Max(value = 180, message = "Longitud debe ser <= 180")
    private Double longitude;

    @Min(value = 100, message = "Radio mínimo: 100 metros")
    @Max(value = 5000, message = "Radio máximo: 5000 metros")
    private Integer radius = 1000; // metros, por defecto 1km

    private List<PoiType> types; // null = todos los tipos

    @Min(value = 1)
    @Max(value = 100)
    private Integer limit = 50; // máximo de resultados
}