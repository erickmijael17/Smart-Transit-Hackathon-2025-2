package com.smarttransit.route.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "routes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Route {
    @Id
    @Column(length = 20)
    private String id; // Ej: "01", "18", "10"

    @Column(length = 120, nullable = false)
    private String name; // Ej: "Ruta 1 - Plaza de Armas"

    @Column(length = 20)
    private String color; // Ej: "#FF6B6B"

    @Column(columnDefinition = "TEXT")
    private String polylineJson; // JSON array de [lat, lon]

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "active")
    private Boolean active = true;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
        updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}


