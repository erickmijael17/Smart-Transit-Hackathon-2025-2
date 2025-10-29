package com.smarttransit.driver.controller;

import com.smarttransit.driver.dto.DriverLoginRequest;
import com.smarttransit.driver.dto.DriverLoginResponse;
import com.smarttransit.driver.entity.Driver;
import com.smarttransit.driver.entity.DriverSession;
import com.smarttransit.driver.repository.DriverRepository;
import com.smarttransit.driver.repository.DriverSessionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/driver")
public class DriverAuthController {

    private final DriverRepository driverRepository;
    private final DriverSessionRepository driverSessionRepository;

    public DriverAuthController(DriverRepository driverRepository, DriverSessionRepository driverSessionRepository) {
        this.driverRepository = driverRepository;
        this.driverSessionRepository = driverSessionRepository;
    }

    @PostMapping("/login")
    @Transactional
    public ResponseEntity<DriverLoginResponse> login(@RequestBody DriverLoginRequest request) {
        String username = request.getUsername() != null ? request.getUsername() : "conductor";

        Driver driver = driverRepository.findByUsername(username)
                .orElseGet(() -> {
                    Driver d = new Driver();
                    d.setUsername(username);
                    d.setDisplayName(username);
                    return driverRepository.save(d);
                });

        String token = UUID.randomUUID().toString();
        DriverSession session = new DriverSession();
        session.setDriver(driver);
        session.setToken(token);
        driverSessionRepository.save(session);

        // Si el driver tiene busId/routeId configurados, usarlos; si no, defaults
        String driverId = driver.getId().toString();
        String busId = driver.getBusId() != null ? driver.getBusId() : "BUS_123";
        String routeId = driver.getRouteId() != null ? driver.getRouteId() : "01";

        DriverLoginResponse response = new DriverLoginResponse(
                driverId,
                driver.getDisplayName(),
                busId,
                routeId,
                true,
                token
        );

        return ResponseEntity.ok(response);
    }
}


