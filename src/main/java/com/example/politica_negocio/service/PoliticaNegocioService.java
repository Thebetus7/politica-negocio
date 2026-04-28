package com.example.politica_negocio.service;

import com.example.politica_negocio.model.PoliticaNegocio;
import com.example.politica_negocio.model.AdminDiagrama;
import com.example.politica_negocio.model.LogDiagrama;
import com.example.politica_negocio.model.Usuario;
import com.example.politica_negocio.repository.AdminDiagramaRepository;
import com.example.politica_negocio.repository.DepartamentoRepository;
import com.example.politica_negocio.repository.LogDiagramaRepository;
import com.example.politica_negocio.repository.PoliticaNegocioRepository;
import com.example.politica_negocio.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PoliticaNegocioService {
    private final PoliticaNegocioRepository repository;
    private final LogDiagramaRepository logDiagramaRepository;
    private final AdminDiagramaRepository adminDiagramaRepository;
    private final UsuarioRepository usuarioRepository;
    private final DepartamentoRepository departamentoRepository;

    public List<PoliticaNegocio> getAll() {
        return repository.findAllActive();
    }

    public PoliticaNegocio getById(String id) {
        return repository.findById(id)
                .filter(p -> p.getDeletedAt() == null)
                .orElse(null);
    }

    public PoliticaNegocio create(PoliticaNegocio politica) {
        politica.setCreatedAt(LocalDateTime.now());
        PoliticaNegocio saved = repository.save(politica);

        LogDiagrama log = new LogDiagrama();
        log.setTiempo(LocalDateTime.now());
        log.setJson(buildInitialDiagramJson(saved));
        log.setCreatedAt(LocalDateTime.now());
        LogDiagrama savedLog = logDiagramaRepository.save(log);

        AdminDiagrama adminDiagrama = new AdminDiagrama();
        adminDiagrama.setPoliticaId(saved.getId());
        adminDiagrama.setLogDiagramaId(savedLog.getId());
        adminDiagrama.setUserId(resolveAuthenticatedUserId());
        adminDiagrama.setPortafolioId(null); // se llenará cuando se integre portafolio
        adminDiagrama.setCreatedAt(LocalDateTime.now());
        adminDiagramaRepository.save(adminDiagrama);

        return saved;
    }

    public PoliticaNegocio update(String id, PoliticaNegocio politica) {
        return repository.findById(id)
                .filter(p -> p.getDeletedAt() == null)
                .map(existing -> {
                    existing.setNombre(politica.getNombre());
                    existing.setDescripcion(politica.getDescripcion());
                    existing.setUpdatedAt(LocalDateTime.now());
                    return repository.save(existing);
                })
                .orElse(null);
    }

    public PoliticaNegocio softDelete(String id) {
        return repository.findById(id)
                .filter(p -> p.getDeletedAt() == null)
                .map(existing -> {
                    existing.setDeletedAt(LocalDateTime.now());
                    return repository.save(existing);
                })
                .orElse(null);
    }

    private String resolveAuthenticatedUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            return null;
        }

        Object principal = auth.getPrincipal();
        if (principal instanceof Usuario usuario) {
            return usuario.getId();
        }
        if (principal instanceof UserDetails userDetails) {
            return usuarioRepository.findByCorreo(userDetails.getUsername())
                    .map(Usuario::getId)
                    .orElse(null);
        }
        return null;
    }

    private Map<String, Object> buildInitialDiagramJson(PoliticaNegocio politica) {
        Map<String, Object> root = new HashMap<>();
        root.put("politicaId", politica.getId());
        root.put("politicaNombre", politica.getNombre());
        root.put("createdAt", LocalDateTime.now().toString());

        List<Map<String, Object>> lanes = departamentoRepository.findAllActive().stream()
                .map(dep -> {
                    Map<String, Object> lane = new HashMap<>();
                    lane.put("departamentoId", dep.getId());
                    lane.put("nombre", dep.getNombre());
                    lane.put("descripcion", dep.getDescripcion());
                    return lane;
                })
                .toList();

        root.put("lanes", lanes);
        root.put("nodes", List.of());
        root.put("links", List.of());
        return root;
    }

}
