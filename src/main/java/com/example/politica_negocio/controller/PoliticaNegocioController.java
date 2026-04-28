package com.example.politica_negocio.controller;

import com.example.politica_negocio.model.PoliticaNegocio;
import com.example.politica_negocio.service.PoliticaNegocioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/politicas")
@RequiredArgsConstructor
public class PoliticaNegocioController {
    private final PoliticaNegocioService service;

    @GetMapping
    public ResponseEntity<List<PoliticaNegocio>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/public")
    public ResponseEntity<List<PoliticaNegocio>> getAllPublic() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PoliticaNegocio> getById(@PathVariable String id) {
        PoliticaNegocio politica = service.getById(id);
        return politica != null ? ResponseEntity.ok(politica) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<PoliticaNegocio> create(@RequestBody PoliticaNegocio politica) {
        return ResponseEntity.ok(service.create(politica));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PoliticaNegocio> update(@PathVariable String id, @RequestBody PoliticaNegocio politica) {
        PoliticaNegocio updated = service.update(id, politica);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<PoliticaNegocio> softDelete(@PathVariable String id) {
        PoliticaNegocio deleted = service.softDelete(id);
        return deleted != null ? ResponseEntity.ok(deleted) : ResponseEntity.notFound().build();
    }
}
