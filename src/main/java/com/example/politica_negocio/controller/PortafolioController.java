package com.example.politica_negocio.controller;

import com.example.politica_negocio.dto.CompletarActividadRequest;
import com.example.politica_negocio.dto.TramiteProgresoDto;
import com.example.politica_negocio.model.Portafolio;
import com.example.politica_negocio.service.PortafolioService;
import com.example.politica_negocio.service.TramiteEngineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/portafolios")
@RequiredArgsConstructor
public class PortafolioController {

    private final PortafolioService service;
    private final TramiteEngineService tramiteEngineService;

    @GetMapping
    public ResponseEntity<List<Portafolio>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Portafolio> getById(@PathVariable String id) {
        Portafolio p = service.getById(id);
        if (p == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(p);
    }

    @GetMapping("/{id}/progreso")
    public ResponseEntity<TramiteProgresoDto> getProgreso(@PathVariable String id) {
        return ResponseEntity.ok(tramiteEngineService.getProgreso(id));
    }

    @PostMapping("/{portafolioId}/actividades/{actividadId}/completar")
    public ResponseEntity<TramiteProgresoDto> completarActividad(
            @PathVariable String portafolioId,
            @PathVariable String actividadId,
            @RequestBody CompletarActividadRequest request) {
        return ResponseEntity.ok(tramiteEngineService.completarActividad(portafolioId, actividadId, request));
    }

    @PostMapping
    public ResponseEntity<Portafolio> create(@RequestBody Portafolio portafolio) {
        return ResponseEntity.ok(service.create(portafolio));
    }
}
