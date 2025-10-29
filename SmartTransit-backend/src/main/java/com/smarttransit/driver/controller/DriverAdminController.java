package com.smarttransit.driver.controller;

import com.smarttransit.driver.dto.DriverDTO;
import com.smarttransit.driver.entity.Driver;
import com.smarttransit.driver.repository.DriverRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/drivers")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class DriverAdminController {

    private final DriverRepository driverRepository;

    /**
     * Obtiene todos los conductores
     */
    @GetMapping
    public ResponseEntity<List<DriverDTO>> getAllDrivers() {
        List<DriverDTO> drivers = driverRepository.findAll().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(drivers);
    }

    /**
     * Crea un nuevo conductor
     */
    @PostMapping
    public ResponseEntity<?> createDriver(@RequestBody DriverDTO driverDTO) {
        if (driverRepository.findByUsername(driverDTO.getUsername()).isPresent()) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Ya existe un conductor con ese username"));
        }

        Driver driver = convertToEntity(driverDTO);
        driver = driverRepository.save(driver);

        return ResponseEntity.ok(Map.of(
            "message", "Conductor creado correctamente",
            "driver", convertToDTO(driver)
        ));
    }

    /**
     * Actualiza un conductor
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateDriver(@PathVariable String id, @RequestBody DriverDTO driverDTO) {
        Driver driver = driverRepository.findById(UUID.fromString(id))
            .orElseThrow(() -> new RuntimeException("Conductor no encontrado"));

        driver.setDisplayName(driverDTO.getDisplayName());
        driver.setBusId(driverDTO.getBusId());
        driver.setRouteId(driverDTO.getRouteId());

        driver = driverRepository.save(driver);

        return ResponseEntity.ok(Map.of(
            "message", "Conductor actualizado correctamente",
            "driver", convertToDTO(driver)
        ));
    }

    /**
     * Elimina un conductor
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDriver(@PathVariable String id) {
        driverRepository.deleteById(UUID.fromString(id));
        return ResponseEntity.ok(Map.of("message", "Conductor eliminado correctamente"));
    }

    private DriverDTO convertToDTO(Driver driver) {
        return new DriverDTO(
            driver.getId().toString(),
            driver.getUsername(),
            driver.getDisplayName(),
            driver.getBusId(),
            driver.getRouteId(),
            driver.getActive() ? "active" : "inactive",
            null, // phone - agregar a entidad si necesario
            null  // email - agregar a entidad si necesario
        );
    }

    private Driver convertToEntity(DriverDTO dto) {
        Driver driver = new Driver();
        driver.setUsername(dto.getUsername());
        driver.setDisplayName(dto.getDisplayName());
        driver.setBusId(dto.getBusId());
        driver.setRouteId(dto.getRouteId());
        driver.setActive("active".equals(dto.getStatus()));
        return driver;
    }
}


