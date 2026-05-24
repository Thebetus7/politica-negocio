package com.example.politica_negocio.service;

import com.example.politica_negocio.model.Portafolio;
import com.example.politica_negocio.model.Flujo;
import com.example.politica_negocio.repository.PortafolioRepository;
import com.example.politica_negocio.repository.FlujoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class PortafolioService {

    private final PortafolioRepository repository;
    private final FlujoRepository flujoRepository;

    public List<Portafolio> getAll() {
        return repository.findAllActive();
    }

    public Portafolio getById(String id) {
        return repository.findById(id).filter(p -> p.getDeletedAt() == null).orElse(null);
    }

    public Portafolio create(Portafolio portafolio) {
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
                    
                    // Solo el nodo inicial o primer orden deberia arrancar "en_progreso" (o lo manejamos en el front/siguiente paso, pero aquí los inicializamos)
                    Object ordenObj = procesoInstancia.get("orden");
                    if (ordenObj != null && String.valueOf(ordenObj).equals("1")) {
                        procesoInstancia.put("estadoActual", "en_progreso");
                    } else {
                        procesoInstancia.put("estadoActual", "pendiente");
                    }
                    instancia.setProceso(procesoInstancia);
                }
                
                flujoRepository.save(instancia);
            }
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
