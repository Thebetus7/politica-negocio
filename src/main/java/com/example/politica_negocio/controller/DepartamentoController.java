package com.example.politica_negocio.controller;

import com.example.politica_negocio.model.Departamento;
import com.example.politica_negocio.service.DepartamentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/departamentos")
@RequiredArgsConstructor
public class DepartamentoController {

    private final DepartamentoService service;

    @GetMapping
    public ResponseEntity<List<Departamento>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/selector")
    public ResponseEntity<List<Map<String, String>>> getForSelector() {
        List<Map<String, String>> data = service.getAll().stream()
                .map(dep -> Map.of(
                        "id", dep.getId(),
                        "nombre", dep.getNombre() == null ? "" : dep.getNombre()
                ))
                .toList();
        return ResponseEntity.ok(data);
    }

    @PostMapping
    public ResponseEntity<Departamento> create(@RequestBody Departamento departamento) {
        return ResponseEntity.ok(service.create(departamento));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Departamento> update(@PathVariable String id, @RequestBody Departamento departamento) {
        Departamento updated = service.update(id, departamento);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Departamento> delete(@PathVariable String id) {
        Departamento deleted = service.softDelete(id);
        return deleted != null ? ResponseEntity.ok(deleted) : ResponseEntity.notFound().build();
    }
}
