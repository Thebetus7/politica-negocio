package com.example.politica_negocio.controller;

import com.example.politica_negocio.dto.LogPoliticaCompileResult;
import com.example.politica_negocio.model.LogPolitica;
import com.example.politica_negocio.service.LogPoliticaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/politicas/{politicaId}/log-politica")
@RequiredArgsConstructor
public class LogPoliticaController {

    private final LogPoliticaService logPoliticaService;

    @PostMapping("/compile")
    public ResponseEntity<LogPoliticaCompileResult> compile(@PathVariable String politicaId) {
        return ResponseEntity.ok(logPoliticaService.compileAndSaveIfValid(politicaId));
    }

    @GetMapping
    public ResponseEntity<LogPolitica> getUltimo(@PathVariable String politicaId) {
        return logPoliticaService.getUltimoValido(politicaId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/historial")
    public ResponseEntity<List<LogPolitica>> getHistorial(@PathVariable String politicaId) {
        return ResponseEntity.ok(logPoliticaService.getHistorial(politicaId));
    }
}
