package com.example.politica_negocio.controller.dto;

import com.example.politica_negocio.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String token;
    private String id;
    private String nombre;
    private String correo;
    private Role rol;
}
