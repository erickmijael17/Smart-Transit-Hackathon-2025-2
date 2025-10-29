package com.smarttransit.admin.repository;

import com.smarttransit.admin.entity.AdminSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AdminSessionRepository extends JpaRepository<AdminSession, UUID> {
    Optional<AdminSession> findByTokenAndActiveTrue(String token);
    void deleteByAdminId(UUID adminId);
    void deleteByExpiresAtBefore(OffsetDateTime now);
}


