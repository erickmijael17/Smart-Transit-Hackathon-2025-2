package com.smarttransit.config;

import com.smarttransit.admin.repository.AdminRepository;
import com.smarttransit.admin.service.AdminAuthService;
import com.smarttransit.driver.entity.Driver;
import com.smarttransit.driver.repository.DriverRepository;
import com.smarttransit.tracking.entity.Bus;
import com.smarttransit.tracking.repository.BusRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    CommandLineRunner seedData(DriverRepository driverRepository, BusRepository busRepository) {
        return args -> {
            // Buses demo con rutas reales del sistema (1, 18, 10)
            if (!busRepository.existsById("BUS_123")) {
                Bus b = new Bus();
                b.setId("BUS_123");
                b.setRouteId("1");
                b.setPlate("ABC-123");
                busRepository.save(b);
            }
            if (!busRepository.existsById("BUS_456")) {
                Bus b = new Bus();
                b.setId("BUS_456");
                b.setRouteId("18");
                b.setPlate("DEF-456");
                busRepository.save(b);
            }
            if (!busRepository.existsById("BUS_789")) {
                Bus b = new Bus();
                b.setId("BUS_789");
                b.setRouteId("10");
                b.setPlate("GHI-789");
                busRepository.save(b);
            }

            // Drivers demo asignados a rutas reales (1, 18, 10)
            seedDriver(driverRepository, "juan", "Juan Pérez", "BUS_123", "1");
            seedDriver(driverRepository, "maria", "María López", "BUS_456", "18");
            seedDriver(driverRepository, "carlos", "Carlos Ruiz", "BUS_789", "10");

            log.info("✓ DataInitializer: conductores y buses demo listos");
        };
    }

    @Bean
    CommandLineRunner seedAdminUser(AdminAuthService adminAuthService, AdminRepository adminRepository) {
        return args -> {
            try {
                // Crear admin demo si no existe
                if (!adminRepository.existsByUsername("admin")) {
                    adminAuthService.createAdmin(
                        "admin",
                        "Administrador Principal",
                        "admin123",
                        "SUPER_ADMIN"
                    );
                    log.info("Admin demo creado exitosamente");
                    log.info("   Username: admin");
                    log.info("   Password: admin123");
                    log.info("   Role: SUPER_ADMIN");
                } else {
                    log.info("Admin 'admin' ya existe en la base de datos");
                }
            } catch (Exception e) {
                log.error("Error al crear admin demo: {}", e.getMessage(), e);
            }
        };
    }

    private void seedDriver(DriverRepository repo, String username, String displayName, String busId, String routeId) {
        repo.findByUsername(username).ifPresentOrElse(d -> {
            if (d.getBusId() == null) d.setBusId(busId);
            if (d.getRouteId() == null) d.setRouteId(routeId);
            if (d.getDisplayName() == null) d.setDisplayName(displayName);
            repo.save(d);
        }, () -> {
            Driver d = new Driver();
            d.setUsername(username);
            d.setDisplayName(displayName);
            d.setBusId(busId);
            d.setRouteId(routeId);
            repo.save(d);
        });
    }
}



