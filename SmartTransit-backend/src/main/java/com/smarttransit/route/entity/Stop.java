package com.smarttransit.route.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "stops")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Stop {
    @Id
    @Column(length = 60)
    private String id;

    @Column(length = 120, nullable = false)
    private String name;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(length = 20)
    private String routeId; // Relación con ruta

    @Column(name = "stop_order")
    private Integer stopOrder; // Orden en la ruta
}


