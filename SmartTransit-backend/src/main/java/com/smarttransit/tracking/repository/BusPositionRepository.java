package com.smarttransit.tracking.repository;

import com.smarttransit.tracking.entity.BusPosition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BusPositionRepository extends JpaRepository<BusPosition, UUID> {
    List<BusPosition> findTop50ByBusIdOrderByTimestampDesc(String busId);
}





