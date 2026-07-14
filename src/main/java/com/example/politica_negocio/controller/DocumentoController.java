package com.example.politica_negocio.controller;

import com.example.politica_negocio.dto.DocumentoResponseDto;
import com.example.politica_negocio.model.Usuario;
import com.example.politica_negocio.model.VersionDocumento;
import com.example.politica_negocio.service.DocumentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/documentos")
@RequiredArgsConstructor
public class DocumentoController {

    private final DocumentoService documentoService;

    private Usuario getUsuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Usuario) {
            return (Usuario) auth.getPrincipal();
        }
        return null;
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<?> subir(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String actividadId,
            @RequestParam(required = false) String portafolioId) {

        Usuario usuario = getUsuarioAutenticado();
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "No autenticado", "code", "UNAUTHORIZED"));
        }
        try {
            DocumentoResponseDto dto = documentoService.subir(file, actividadId, portafolioId, usuario.getId());
            return ResponseEntity.ok(dto);
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode())
                    .body(Map.of("message", ex.getReason() != null ? ex.getReason() : ex.getMessage(), "code", "ERROR"));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error interno al subir el archivo: " + ex.getMessage(), "code", "SERVER_ERROR"));
        }
    }

    @GetMapping
    public ResponseEntity<List<DocumentoResponseDto>> listar(
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false, defaultValue = "true") boolean ordenFechaDesc) {
        return ResponseEntity.ok(documentoService.listar(busqueda, estado, ordenFechaDesc));
    }

    @GetMapping("/politica/{politicaId}")
    public ResponseEntity<List<DocumentoResponseDto>> listarAceptadosPorPolitica(@PathVariable String politicaId) {
        return ResponseEntity.ok(documentoService.listarAceptadosPorPolitica(politicaId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentoResponseDto> getById(@PathVariable String id) {
        return ResponseEntity.ok(documentoService.getDto(id));
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(
            @PathVariable String id,
            @RequestBody Map<String, String> payload) {
        Usuario usuario = getUsuarioAutenticado();
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "No autenticado", "code", "UNAUTHORIZED"));
        }
        String nuevoEstado = payload.get("estado");
        if (nuevoEstado == null || nuevoEstado.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Estado no provisto", "code", "BAD_REQUEST"));
        }
        try {
            return ResponseEntity.ok(documentoService.cambiarEstado(id, nuevoEstado));
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode())
                    .body(Map.of("message", ex.getReason() != null ? ex.getReason() : ex.getMessage(), "code", "ERROR"));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", ex.getMessage(), "code", "SERVER_ERROR"));
        }
    }

    @PutMapping("/{id}/contenido")
    public ResponseEntity<?> actualizarContenido(
            @PathVariable String id,
            @RequestBody Map<String, String> payload) {
        Usuario usuario = getUsuarioAutenticado();
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "No autenticado", "code", "UNAUTHORIZED"));
        }
        String contenido = payload.get("contenido");
        try {
            return ResponseEntity.ok(documentoService.actualizarContenidoTexto(id, contenido));
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode())
                    .body(Map.of("message", ex.getReason() != null ? ex.getReason() : ex.getMessage(), "code", "ERROR"));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", ex.getMessage(), "code", "SERVER_ERROR"));
        }
    }

    @GetMapping("/{id}/versiones")
    public ResponseEntity<List<VersionDocumento>> obtenerVersiones(@PathVariable String id) {
        return ResponseEntity.ok(documentoService.obtenerVersiones(id));
    }

    @PostMapping("/{id}/versiones")
    public ResponseEntity<?> crearVersion(
            @PathVariable String id,
            @RequestBody Map<String, String> payload) {
        Usuario usuario = getUsuarioAutenticado();
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "No autenticado", "code", "UNAUTHORIZED"));
        }
        String comentario = payload.get("comentario");
        try {
            return ResponseEntity.ok(documentoService.crearNuevaVersion(id, comentario, usuario.getId()));
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode())
                    .body(Map.of("message", ex.getReason() != null ? ex.getReason() : ex.getMessage(), "code", "ERROR"));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", ex.getMessage(), "code", "SERVER_ERROR"));
        }
    }

    @PostMapping("/{id}/revertir/{versionId}")
    public ResponseEntity<?> revertirVersion(
            @PathVariable String id,
            @PathVariable String versionId) {
        Usuario usuario = getUsuarioAutenticado();
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "No autenticado", "code", "UNAUTHORIZED"));
        }
        try {
            return ResponseEntity.ok(documentoService.revertirAVersion(id, versionId, usuario.getId()));
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode())
                    .body(Map.of("message", ex.getReason() != null ? ex.getReason() : ex.getMessage(), "code", "ERROR"));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", ex.getMessage(), "code", "SERVER_ERROR"));
        }
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
