package com.example.politica_negocio.controller;

import com.example.politica_negocio.model.Actividad;
import com.example.politica_negocio.service.ActividadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/politicas/{politicaId}/actividades")
@RequiredArgsConstructor
public class ActividadController {

    private final ActividadService service;

    @GetMapping
    public ResponseEntity<List<Actividad>> getByPolitica(@PathVariable String politicaId) {
        return ResponseEntity.ok(service.getByPoliticaId(politicaId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Actividad> getById(@PathVariable String id) {
        Actividad actividad = service.getById(id);
        return actividad != null ? ResponseEntity.ok(actividad) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<Actividad> create(@PathVariable String politicaId, @RequestBody Actividad actividad) {
        actividad.setPoliticaId(politicaId);
        return ResponseEntity.ok(service.create(actividad));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Actividad> update(@PathVariable String id, @RequestBody Actividad actividad) {
        Actividad updated = service.update(id, actividad);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Actividad> softDelete(@PathVariable String id) {
        Actividad deleted = service.softDelete(id);
        return deleted != null ? ResponseEntity.ok(deleted) : ResponseEntity.notFound().build();
    }
}
