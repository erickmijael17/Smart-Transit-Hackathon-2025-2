package com.smarttransit.driver.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "drivers")
@Getter
@Setter
public class Driver {
    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false, unique = true, length = 80)
    private String username;

    @Column(nullable = false, length = 120)
    private String displayName;

    @Column(length = 200)
    private String passwordHash; // opcional, placeholder por ahora

    @Column(nullable = false)
    private Boolean active = true;

    @Column(length = 60)
    private String busId;

    @Column(length = 20)
    private String routeId;

    @CreationTimestamp
    private OffsetDateTime createdAt;
}


