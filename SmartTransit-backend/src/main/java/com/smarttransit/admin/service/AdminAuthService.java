package com.smarttransit.admin.service;

import com.smarttransit.admin.dto.AdminLoginRequest;
import com.smarttransit.admin.dto.AdminLoginResponse;
import com.smarttransit.admin.entity.Admin;
import com.smarttransit.admin.entity.AdminSession;
import com.smarttransit.admin.repository.AdminRepository;
import com.smarttransit.admin.repository.AdminSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminAuthService {

    private final AdminRepository adminRepository;
    private final AdminSessionRepository adminSessionRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    /**
     * Autentica un administrador y crea una sesión
     */
    @Transactional
    public AdminLoginResponse login(AdminLoginRequest request) {
        // Buscar admin por username
        Admin admin = adminRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new RuntimeException("Usuario o contraseña incorrectos"));

        // Verificar que esté activo
        if (!admin.getActive()) {
            throw new RuntimeException("Usuario inactivo");
        }

        // Verificar contraseña
        if (!passwordEncoder.matches(request.getPassword(), admin.getPasswordHash())) {
            throw new RuntimeException("Usuario o contraseña incorrectos");
        }

        // Actualizar último login
        admin.setLastLogin(OffsetDateTime.now());
        adminRepository.save(admin);

        // Crear sesión
        AdminSession session = new AdminSession();
        session.setAdminId(admin.getId());
        session.setToken(UUID.randomUUID().toString());
        session.setActive(true);
        session = adminSessionRepository.save(session);

        log.info("Admin {} ha iniciado sesión", admin.getUsername());

        // Retornar respuesta
        return new AdminLoginResponse(
            admin.getId().toString(),
            admin.getUsername(),
            admin.getFullName(),
            admin.getRole(),
            session.getToken()
        );
    }

    /**
     * Cierra sesión de un administrador
     */
    @Transactional
    public void logout(String token) {
        Optional<AdminSession> session = adminSessionRepository.findByTokenAndActiveTrue(token);
        session.ifPresent(s -> {
            s.setActive(false);
            adminSessionRepository.save(s);
            log.info("Sesión cerrada para token: {}", token);
        });
    }

    /**
     * Valida un token de sesión
     */
    public boolean validateToken(String token) {
        Optional<AdminSession> session = adminSessionRepository.findByTokenAndActiveTrue(token);
        
        if (session.isEmpty()) {
            return false;
        }

        AdminSession s = session.get();
        
        // Verificar que no haya expirado
        if (s.getExpiresAt().isBefore(OffsetDateTime.now())) {
            s.setActive(false);
            adminSessionRepository.save(s);
            return false;
        }

        return true;
    }

    /**
     * Obtiene el admin asociado a un token
     */
    public Optional<Admin> getAdminByToken(String token) {
        return adminSessionRepository.findByTokenAndActiveTrue(token)
            .filter(s -> s.getExpiresAt().isAfter(OffsetDateTime.now()))
            .flatMap(s -> adminRepository.findById(s.getAdminId()));
    }

    /**
     * Crea un administrador (usar solo para setup inicial)
     */
    @Transactional
    public Admin createAdmin(String username, String fullName, String password, String role) {
        if (adminRepository.existsByUsername(username)) {
            throw new RuntimeException("El username ya existe");
        }

        Admin admin = new Admin();
        admin.setUsername(username);
        admin.setFullName(fullName);
        admin.setPasswordHash(passwordEncoder.encode(password));
        admin.setRole(role != null ? role : "ADMIN");
        admin.setActive(true);

        admin = adminRepository.save(admin);
        log.info("Administrador creado: {}", username);

        return admin;
    }

    /**
     * Limpia sesiones expiradas (ejecutar periódicamente)
     */
    @Transactional
    public void cleanExpiredSessions() {
        adminSessionRepository.deleteByExpiresAtBefore(OffsetDateTime.now());
        log.info("Sesiones expiradas limpiadas");
    }
}


