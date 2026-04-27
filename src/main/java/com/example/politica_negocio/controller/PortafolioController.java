package com.example.politica_negocio.controller;

import com.example.politica_negocio.model.Portafolio;
import com.example.politica_negocio.service.PortafolioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/portafolios")
@RequiredArgsConstructor
public class PortafolioController {

    private final PortafolioService service;

    @GetMapping
    public ResponseEntity<List<Portafolio>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @PostMapping
    public ResponseEntity<Portafolio> create(@RequestBody Portafolio portafolio) {
        return ResponseEntity.ok(service.create(portafolio));
    }
}
