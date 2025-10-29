package com.smarttransit.admin.controller;

import com.smarttransit.admin.dto.AdminLoginRequest;
import com.smarttransit.admin.dto.AdminLoginResponse;
import com.smarttransit.admin.service.AdminAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:4200")
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AdminLoginRequest request) {
        try {
            if (request.getUsername() == null || request.getPassword() == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Username y password son requeridos"));
            }

            AdminLoginResponse response = adminAuthService.login(request);
            
            log.info("Login exitoso para admin: {}", request.getUsername());
            
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.warn("Intento de login fallido para: {}", request.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authorization) {
        try {
            String token = authorization.replace("Bearer ", "");
            adminAuthService.logout(token);
            
            return ResponseEntity.ok(Map.of("message", "Sesión cerrada correctamente"));

        } catch (Exception e) {
            log.error("Error al cerrar sesión", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error al cerrar sesión"));
        }
    }

    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authorization) {
        try {
            String token = authorization.replace("Bearer ", "");
            boolean valid = adminAuthService.validateToken(token);
            
            if (valid) {
                return ResponseEntity.ok(Map.of("valid", true));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("valid", false));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("valid", false));
        }
    }
}


