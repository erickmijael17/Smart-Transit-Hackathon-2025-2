package com.smarttransit.driver.repository;

import com.smarttransit.driver.entity.DriverSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DriverSessionRepository extends JpaRepository<DriverSession, UUID> {
    Optional<DriverSession> findByTokenAndActiveTrue(String token);
}





