package com.example.politica_negocio.controller;

import com.example.politica_negocio.model.Flujo;
import com.example.politica_negocio.service.FlujoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/politicas/{politicaId}/flujos")
@RequiredArgsConstructor
public class FlujoController {

    private final FlujoService service;

    @GetMapping
    public ResponseEntity<List<Flujo>> getByPolitica(@PathVariable String politicaId) {
        return ResponseEntity.ok(service.getByPoliticaId(politicaId));
    }

    @PostMapping
    public ResponseEntity<Flujo> create(@PathVariable String politicaId, @RequestBody Flujo flujo) {
        flujo.setPoliticaId(politicaId);
        return ResponseEntity.ok(service.create(flujo));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Flujo> update(@PathVariable String id, @RequestBody Flujo flujo) {
        Flujo updated = service.update(id, flujo);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Flujo> softDelete(@PathVariable String id) {
        Flujo deleted = service.softDelete(id);
        return deleted != null ? ResponseEntity.ok(deleted) : ResponseEntity.notFound().build();
    }
}
