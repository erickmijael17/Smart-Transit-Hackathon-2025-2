package com.smarttransit.admin.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "admins")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Admin {
    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(length = 80, unique = true, nullable = false)
    private String username;

    @Column(length = 120, nullable = false)
    private String fullName;

    @Column(length = 200)
    private String passwordHash; // BCrypt hash

    @Column(length = 50)
    private String role; // "ADMIN", "SUPER_ADMIN"

    @Column(length = 120)
    private String email;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "last_login")
    private OffsetDateTime lastLogin;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
    }
}


