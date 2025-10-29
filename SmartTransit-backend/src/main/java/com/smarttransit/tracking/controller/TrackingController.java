package com.smarttransit.tracking.controller;

import com.smarttransit.tracking.dto.BusPositionDTO;
import com.smarttransit.tracking.entity.BusPosition;
import com.smarttransit.tracking.entity.CurrentBusState;
import com.smarttransit.tracking.mapper.BusPositionMapper;
import com.smarttransit.tracking.repository.BusPositionRepository;
import com.smarttransit.tracking.repository.CurrentBusStateRepository;
import com.smarttransit.tracking.service.LiveTrackingService;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/tracking")
public class TrackingController {

    private final LiveTrackingService liveTrackingService;
    private final BusPositionRepository busPositionRepository;
    private final CurrentBusStateRepository currentBusStateRepository;
    private final BusPositionMapper busPositionMapper;

    public TrackingController(LiveTrackingService liveTrackingService,
                              BusPositionRepository busPositionRepository,
                              CurrentBusStateRepository currentBusStateRepository,
                              BusPositionMapper busPositionMapper) {
        this.liveTrackingService = liveTrackingService;
        this.busPositionRepository = busPositionRepository;
        this.currentBusStateRepository = currentBusStateRepository;
        this.busPositionMapper = busPositionMapper;
    }

    /**
     * Recibe actualizaciones de ubicación publicadas por la app del conductor,
     * las persiste y las retransmite por WebSocket a los suscriptores.
     */
    @PostMapping("/position")
    @Transactional
    public ResponseEntity<Void> publishPosition(@RequestBody BusPositionDTO position) {
        BusPosition entity = busPositionMapper.toEntity(position);
        busPositionRepository.save(entity);

        CurrentBusState state = busPositionMapper.toCurrentState(position);
        state.setBusId(position.getBusId());
        currentBusStateRepository.save(state);

        liveTrackingService.publishExternalPosition(position);
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("tracking-ok");
    }
}
