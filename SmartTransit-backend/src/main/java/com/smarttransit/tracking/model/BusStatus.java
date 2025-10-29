package com.smarttransit.tracking.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum BusStatus {
    ACTIVE("ACTIVE"),                   // Bus en servicio activo
    DELAYED("DELAYED"),                 // Bus retrasado
    STOPPED("STOPPED"),                 // Bus detenido temporalmente
    COMPLETED("COMPLETED"),             // Bus completó recorrido
    OUT_OF_SERVICE("OUT_OF_SERVICE");   // Bus fuera de servicio

    private final String value;

    BusStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}