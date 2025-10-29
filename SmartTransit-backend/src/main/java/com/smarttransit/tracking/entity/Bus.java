package com.smarttransit.tracking.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "buses")
@Getter
@Setter
public class Bus {
    @Id
    @Column(length = 60)
    private String id; // ej: BUS_123 o BUS-18-01

    @Column(length = 20)
    private String routeId; // solo referencia string; rutas siguen en caché

    @Column(length = 60)
    private String plate;
}





