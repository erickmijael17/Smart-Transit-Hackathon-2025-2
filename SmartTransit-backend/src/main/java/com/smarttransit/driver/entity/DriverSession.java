package com.smarttransit.driver.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "driver_sessions")
@Getter
@Setter
public class DriverSession {
    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Driver driver;

    @Column(nullable = false, unique = true, length = 200)
    private String token;

    @CreationTimestamp
    private OffsetDateTime createdAt;

    private OffsetDateTime expiresAt; // opcional

    @Column(nullable = false)
    private boolean active = true;
}





