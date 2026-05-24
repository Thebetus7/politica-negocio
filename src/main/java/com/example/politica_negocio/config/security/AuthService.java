package com.example.politica_negocio.config.security;

import com.example.politica_negocio.controller.dto.AuthRequest;
import com.example.politica_negocio.controller.dto.AuthResponse;
import com.example.politica_negocio.controller.dto.RegisterRequest;
import com.example.politica_negocio.exception.LoginAuthException;
import com.example.politica_negocio.model.Role;
import com.example.politica_negocio.model.Usuario;
import com.example.politica_negocio.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponse register(RegisterRequest request) {
        var user = new Usuario();
        user.setNombre(request.getNombre());
        user.setCorreo(request.getCorreo());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        // Registro por defecto crea un administrador
        user.setRol(Role.ADMINISTRADOR);
        user.setCreatedAt(LocalDateTime.now());
        
        repository.save(user);
        
        var jwtToken = jwtService.generateToken(user);
        return AuthResponse.builder()
                .token(jwtToken)
                .id(user.getId())
                .nombre(user.getNombre())
                .correo(user.getCorreo())
                .rol(user.getRol())
                .build();
    }

    public AuthResponse authenticate(AuthRequest request) {
        var user = repository.findByCorreo(request.getCorreo())
                .orElseThrow(() -> new LoginAuthException(
                        HttpStatus.NOT_FOUND,
                        "EMAIL_NOT_FOUND",
                        "No existe una cuenta registrada con ese correo electrónico."
                ));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new LoginAuthException(
                    HttpStatus.UNAUTHORIZED,
                    "WRONG_PASSWORD",
                    "La contraseña es incorrecta."
            );
        }

        var jwtToken = jwtService.generateToken(user);
        return AuthResponse.builder()
                .token(jwtToken)
                .id(user.getId())
                .nombre(user.getNombre())
                .correo(user.getCorreo())
                .rol(user.getRol())
                .build();
    }

    public boolean existsByCorreo(String correo) {
        return repository.findByCorreo(correo).isPresent();
    }
}
