package com.example.politica_negocio.controller;

import com.example.politica_negocio.model.FuncionarioDepa;
import com.example.politica_negocio.service.FuncionarioDepaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/funcionarios-depa")
@RequiredArgsConstructor
public class FuncionarioDepaController {

    private final FuncionarioDepaService service;

    @GetMapping
    public ResponseEntity<List<FuncionarioDepa>> getByDepartamento(
            @RequestParam String departamentoId) {
        return ResponseEntity.ok(service.getByDepartamentoId(departamentoId));
    }

    @GetMapping("/usuario/{userId}")
    public ResponseEntity<List<FuncionarioDepa>> getByUsuario(@PathVariable String userId) {
        return ResponseEntity.ok(service.getByUserId(userId));
    }

    @PostMapping
    public ResponseEntity<FuncionarioDepa> create(@RequestBody FuncionarioDepa funcionarioDepa) {
        return ResponseEntity.ok(service.create(funcionarioDepa));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<FuncionarioDepa> softDelete(@PathVariable String id) {
        FuncionarioDepa deleted = service.softDelete(id);
        return deleted != null ? ResponseEntity.ok(deleted) : ResponseEntity.notFound().build();
    }
}
