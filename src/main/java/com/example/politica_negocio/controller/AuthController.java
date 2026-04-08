package com.example.politica_negocio.controller;

import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> credentials) {
        Map<String, Object> response = new HashMap<>();
        String username = credentials.get("username");
        String password = credentials.get("password");

        // Simulación básica. Cualquier acceso con admin123 pasa
        if ("admin123".equals(password)) {
            response.put("status", "success");
            response.put("token", username + "-dummy-token"); 
            response.put("user", Map.of(
                "id", 1,
                "username", username != null ? username : "Usuario",
                "roles", List.of("Administrador", "Auxiliar")
            ));
        } else {
            response.put("status", "error");
            response.put("message", "Credenciales incorrectas");
        }

        return response;
    }

    @GetMapping("/me")
    public Map<String, Object> me(@RequestHeader(value="Authorization", required=false) String token) {
        // En un flujo real aquí se decodifica el JWT. Por ahora retornamos el profile mock.
        return Map.of(
            "id", 1,
            "username", token != null ? token.replace("-dummy-token", "").replace("Bearer ", "") : "Admin",
            "roles", List.of("Administrador", "Auxiliar")
        );
    }
}
