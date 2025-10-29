package com.smarttransit.bus.controller;

import com.smarttransit.bus.dto.BusDTO;
import com.smarttransit.tracking.entity.Bus;
import com.smarttransit.tracking.repository.BusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/buses")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class BusAdminController {

    private final BusRepository busRepository;

    /**
     * Obtiene todos los buses
     */
    @GetMapping
    public ResponseEntity<List<BusDTO>> getAllBuses() {
        List<BusDTO> buses = busRepository.findAll().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(buses);
    }

    /**
     * Crea un nuevo bus
     */
    @PostMapping
    public ResponseEntity<?> createBus(@RequestBody BusDTO busDTO) {
        if (busRepository.existsById(busDTO.getId())) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Ya existe un bus con el ID: " + busDTO.getId()));
        }

        Bus bus = convertToEntity(busDTO);
        bus = busRepository.save(bus);

        return ResponseEntity.ok(Map.of(
            "message", "Bus creado correctamente",
            "bus", convertToDTO(bus)
        ));
    }

    /**
     * Actualiza un bus
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateBus(@PathVariable String id, @RequestBody BusDTO busDTO) {
        Bus bus = busRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Bus no encontrado"));

        bus.setPlate(busDTO.getPlate());
        bus.setRouteId(busDTO.getRouteId());

        bus = busRepository.save(bus);

        return ResponseEntity.ok(Map.of(
            "message", "Bus actualizado correctamente",
            "bus", convertToDTO(bus)
        ));
    }

    /**
     * Elimina un bus
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBus(@PathVariable String id) {
        busRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Bus eliminado correctamente"));
    }

    private BusDTO convertToDTO(Bus bus) {
        return new BusDTO(
            bus.getId(),
            bus.getPlate(),
            bus.getRouteId(),
            null, // capacity - agregar a entidad si necesario
            "active", // status - agregar a entidad si necesario
            null // assignedDriver - obtener de relación
        );
    }

    private Bus convertToEntity(BusDTO dto) {
        Bus bus = new Bus();
        bus.setId(dto.getId());
        bus.setPlate(dto.getPlate());
        bus.setRouteId(dto.getRouteId());
        return bus;
    }
}


