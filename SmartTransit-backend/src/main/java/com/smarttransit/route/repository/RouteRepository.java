package com.smarttransit.route.repository;

import com.smarttransit.route.entity.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RouteRepository extends JpaRepository<Route, String> {
    List<Route> findByActiveTrue();
    boolean existsById(String id);
}


