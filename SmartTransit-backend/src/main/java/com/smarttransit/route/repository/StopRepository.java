package com.smarttransit.route.repository;

import com.smarttransit.route.entity.Stop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StopRepository extends JpaRepository<Stop, String> {
    List<Stop> findByRouteIdOrderByStopOrder(String routeId);
    void deleteByRouteId(String routeId);
}


