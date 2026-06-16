package com.example.politica_negocio.controller;

import com.example.politica_negocio.model.Usuario;
import com.example.politica_negocio.service.VozFormularioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/voz")
@RequiredArgsConstructor
public class VozController {

    private final VozFormularioService vozFormularioService;

    @PostMapping("/llenar-formulario/{formularioId}")
    public ResponseEntity<Map<String, Object>> llenarFormulario(
            @PathVariable String formularioId,
            @RequestParam("audio") MultipartFile audio,
            @AuthenticationPrincipal Usuario usuario) {
        if (usuario == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autenticado");
        }
        return ResponseEntity.ok(vozFormularioService.llenarDesdeAudio(formularioId, audio));
    }
}
