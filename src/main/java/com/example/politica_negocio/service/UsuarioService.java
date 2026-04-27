package com.example.politica_negocio.service;

import com.example.politica_negocio.model.Usuario;
import com.example.politica_negocio.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;

    public List<Usuario> getAllUsuarios() {
        return repository.findAllActive();
    }

    public Usuario getUsuarioById(String id) {
        return repository.findById(id).filter(u -> u.getDeletedAt() == null).orElse(null);
    }

    public Usuario createUsuario(Usuario usuario) {
        usuario.setCreatedAt(LocalDateTime.now());
        // Encriptar password
        if (usuario.getPassword() != null && !usuario.getPassword().isEmpty()) {
            usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        }
        return repository.save(usuario);
    }

    public Usuario softDelete(String id) {
        Usuario usuario = getUsuarioById(id);
        if (usuario != null) {
            usuario.setDeletedAt(LocalDateTime.now());
            return repository.save(usuario);
        }
        return null;
    }
}
