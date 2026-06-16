package com.example.politica_negocio.controller;

import com.example.politica_negocio.dto.DocumentoResponseDto;
import com.example.politica_negocio.model.Usuario;
import com.example.politica_negocio.service.DocumentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;

@RestController
@RequestMapping("/api/documentos")
@RequiredArgsConstructor
public class DocumentoController {

    private final DocumentoService documentoService;

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<DocumentoResponseDto> subir(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String actividadId,
            @RequestParam(required = false) String portafolioId,
            @AuthenticationPrincipal Usuario usuario) {

        if (usuario == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autenticado");
        }
        DocumentoResponseDto dto = documentoService.subir(file, actividadId, portafolioId, usuario.getId());
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentoResponseDto> getById(@PathVariable String id) {
        return ResponseEntity.ok(documentoService.getDto(id));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Void> download(@PathVariable String id) {
        String url = documentoService.presignedDownloadUrl(id);
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(url)).build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        boolean ok = documentoService.softDelete(id);
        return ok ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }
}
