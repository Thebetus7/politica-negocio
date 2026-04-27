package com.example.politica_negocio.controller;

import com.example.politica_negocio.model.Usuario;
import com.example.politica_negocio.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService service;

    @GetMapping
    public ResponseEntity<List<Usuario>> getAll() {
        return ResponseEntity.ok(service.getAllUsuarios());
    }

    @PostMapping
    public ResponseEntity<Usuario> create(@RequestBody Usuario usuario) {
        return ResponseEntity.ok(service.createUsuario(usuario));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Usuario> delete(@PathVariable String id) {
        Usuario deleted = service.softDelete(id);
        return deleted != null ? ResponseEntity.ok(deleted) : ResponseEntity.notFound().build();
    }
}
