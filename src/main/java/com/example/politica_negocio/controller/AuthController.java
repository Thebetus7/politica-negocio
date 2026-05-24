package com.example.politica_negocio.controller;

import com.example.politica_negocio.config.security.AuthService;
import com.example.politica_negocio.controller.dto.ApiErrorResponse;
import com.example.politica_negocio.controller.dto.AuthRequest;
import com.example.politica_negocio.controller.dto.AuthResponse;
import com.example.politica_negocio.controller.dto.RegisterRequest;
import com.example.politica_negocio.exception.LoginAuthException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService service;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @RequestBody RegisterRequest request
    ) {
        return ResponseEntity.ok(service.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticate(
            @RequestBody AuthRequest request
    ) {
        try {
            return ResponseEntity.ok(service.authenticate(request));
        } catch (LoginAuthException ex) {
            return ResponseEntity
                    .status(ex.getStatus())
                    .body(ApiErrorResponse.builder()
                            .message(ex.getMessage())
                            .code(ex.getCode())
                            .build());
        }
    }

    @GetMapping("/exists")
    public ResponseEntity<Map<String, Boolean>> existsByCorreo(
            @RequestParam String correo
    ) {
        return ResponseEntity.ok(Map.of("exists", service.existsByCorreo(correo)));
    }
}
