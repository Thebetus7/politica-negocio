package com.example.politica_negocio.controller;

import com.example.politica_negocio.dto.TareaFuncionarioDto;
import com.example.politica_negocio.service.TramiteEngineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/funcionarios")
@RequiredArgsConstructor
public class FuncionarioTareaController {

    private final TramiteEngineService tramiteEngineService;

    @GetMapping("/{userId}/tareas")
    public ResponseEntity<List<TareaFuncionarioDto>> getTareas(@PathVariable String userId) {
        return ResponseEntity.ok(tramiteEngineService.getTareasFuncionario(userId));
    }
}
