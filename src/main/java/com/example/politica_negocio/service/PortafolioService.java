package com.example.politica_negocio.service;

import com.example.politica_negocio.model.Portafolio;
import com.example.politica_negocio.model.Flujo;
import com.example.politica_negocio.repository.FlujoRepository;
import com.example.politica_negocio.repository.PortafolioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PortafolioService {

    private final PortafolioRepository repository;
    private final FlujoRepository flujoRepository;
    private final TramiteEngineService tramiteEngineService;

    public List<Portafolio> getAll() {
        return repository.findAllActive();
    }

    public Portafolio getById(String id) {
        return repository.findById(id).filter(p -> p.getDeletedAt() == null).orElse(null);
    }

    public Portafolio create(Portafolio portafolio) {
        if (portafolio.getPoliticaId() == null || portafolio.getPoliticaId().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "politicaId es requerido");
        }
        portafolio.setCreatedAt(LocalDateTime.now());
        if (portafolio.getEstado() == null) {
            portafolio.setEstado("en_progreso");
        }
        Portafolio saved = repository.save(portafolio);

        if (saved.getPoliticaId() != null) {
            List<Flujo> plantillas = flujoRepository.findPlantillasByPoliticaId(saved.getPoliticaId());
            for (Flujo plantilla : plantillas) {
                Flujo instancia = new Flujo();
                instancia.setPoliticaId(plantilla.getPoliticaId());
                instancia.setActividadId(plantilla.getActividadId());
                instancia.setPortafolioId(saved.getId());
                instancia.setCreatedAt(LocalDateTime.now());

                if (plantilla.getProceso() != null) {
                    Map<String, Object> procesoInstancia = new HashMap<>(plantilla.getProceso());
                    procesoInstancia.put("estadoActual", "pendiente");
                    instancia.setProceso(procesoInstancia);
                }

                flujoRepository.save(instancia);
            }

            tramiteEngineService.iniciarFlujoDesdeInicio(saved);
        }

        return saved;
    }

    public Portafolio update(String id, Portafolio portafolio) {
        return repository.findById(id)
                .filter(p -> p.getDeletedAt() == null)
                .map(existing -> {
                    existing.setJson(portafolio.getJson());
                    existing.setUpdatedAt(LocalDateTime.now());
                    return repository.save(existing);
                })
                .orElse(null);
    }
}
