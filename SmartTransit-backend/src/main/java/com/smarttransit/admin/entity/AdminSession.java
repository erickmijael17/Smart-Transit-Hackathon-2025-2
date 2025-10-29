package com.smarttransit.admin.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "admin_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminSession {
    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "admin_id", nullable = false)
    private UUID adminId;

    @Column(length = 200, unique = true, nullable = false)
    private String token;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "expires_at")
    private OffsetDateTime expiresAt;

    @Column(nullable = false)
    private Boolean active = true;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
        // Sesión válida por 24 horas
        expiresAt = OffsetDateTime.now().plusHours(24);
    }
}


