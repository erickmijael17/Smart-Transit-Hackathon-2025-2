package com.smarttransit.pois.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum PoiType {
    // Comercios
    SHOP("Tienda"),
    SUPERMARKET("Supermercado"),
    MALL("Centro Comercial"),
    PHARMACY("Farmacia"),

    // Alimentación
    RESTAURANT("Restaurante"),
    CAFE("Cafetería"),
    FAST_FOOD("Comida Rápida"),
    BAR("Bar"),

    // Servicios
    BANK("Banco"),
    ATM("Cajero Automático"),
    HOSPITAL("Hospital"),
    CLINIC("Clínica"),

    // Transporte
    BUS_STOP("Parada de Bus"),
    PARKING("Estacionamiento"),
    FUEL("Gasolinera"),

    // Educación
    SCHOOL("Colegio"),
    UNIVERSITY("Universidad"),
    LIBRARY("Biblioteca"),

    // Entretenimiento
    CINEMA("Cine"),
    THEATRE("Teatro"),
    PARK("Parque"),

    // Gobierno
    POLICE("Policía"),
    FIRE_STATION("Bomberos"),
    POST_OFFICE("Oficina Postal"),

    // Otros
    HOTEL("Hotel"),
    CHURCH("Iglesia"),
    OTHER("Otro");

    private final String displayName;

    PoiType(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }
}