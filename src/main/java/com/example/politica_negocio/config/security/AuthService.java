package com.example.politica_negocio.config.security;

import com.example.politica_negocio.controller.dto.AuthRequest;
import com.example.politica_negocio.controller.dto.AuthResponse;
import com.example.politica_negocio.controller.dto.RegisterRequest;
import com.example.politica_negocio.model.Role;
import com.example.politica_negocio.model.Usuario;
import com.example.politica_negocio.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

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
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getCorreo(),
                        request.getPassword()
                )
        );
        var user = repository.findByCorreo(request.getCorreo())
                .orElseThrow();
                
        var jwtToken = jwtService.generateToken(user);
        return AuthResponse.builder()
                .token(jwtToken)
                .id(user.getId())
                .nombre(user.getNombre())
                .correo(user.getCorreo())
                .rol(user.getRol())
                .build();
    }
}
