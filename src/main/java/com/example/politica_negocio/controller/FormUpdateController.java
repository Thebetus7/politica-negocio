package com.example.politica_negocio.controller;

import com.example.politica_negocio.model.FormUpdate;
import com.example.politica_negocio.service.FormUpdateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/form-updates")
@RequiredArgsConstructor
public class FormUpdateController {

    private final FormUpdateService service;

    @GetMapping
    public ResponseEntity<List<FormUpdate>> getByActividad(@RequestParam String actividadId) {
        return ResponseEntity.ok(service.getByActividadId(actividadId));
    }

    @PostMapping
    public ResponseEntity<FormUpdate> create(@RequestBody FormUpdate formUpdate) {
        return ResponseEntity.ok(service.create(formUpdate));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FormUpdate> update(@PathVariable String id, @RequestBody FormUpdate formUpdate) {
        FormUpdate updated = service.update(id, formUpdate);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }
}
