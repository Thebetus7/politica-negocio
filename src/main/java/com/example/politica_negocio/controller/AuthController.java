package com.example.politica_negocio.controller;

import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    // Login genérico estructurado para flujo de prueba.
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> credentials) {
        Map<String, Object> response = new HashMap<>();
        String username = credentials.get("username");
        String password = credentials.get("password");

        if ("admin".equals(username) && "admin123".equals(password)) {
            response.put("status", "success");
            response.put("token", "dummy-jwt-token-hA8fJs9"); 
            response.put("user_id", 1);
            response.put("username", "Administrador Pruebas");
        } else {
            response.put("status", "error");
            response.put("message", "Credenciales incorrectas");
        }

        return response;
    }
}
