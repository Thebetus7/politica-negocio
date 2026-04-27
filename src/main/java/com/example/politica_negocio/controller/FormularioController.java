package com.example.politica_negocio.controller;

import com.example.politica_negocio.model.Formulario;
import com.example.politica_negocio.service.FormularioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/formularios")
@RequiredArgsConstructor
public class FormularioController {

    private final FormularioService service;

    @GetMapping
    public ResponseEntity<List<Formulario>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Formulario> getById(@PathVariable String id) {
        Formulario f = service.getById(id);
        return f != null ? ResponseEntity.ok(f) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<Formulario> create(@RequestBody Formulario formulario) {
        return ResponseEntity.ok(service.create(formulario));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Formulario> update(@PathVariable String id, @RequestBody Formulario formulario) {
        Formulario actualizado = service.update(id, formulario);
        return actualizado != null ? ResponseEntity.ok(actualizado) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDelete(@PathVariable String id) {
        boolean ok = service.softDelete(id);
        return ok ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }
}
