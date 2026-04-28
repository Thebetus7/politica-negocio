package com.example.politica_negocio.service;

import com.example.politica_negocio.model.Flujo;
import com.example.politica_negocio.repository.FlujoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FlujoService {

    private final FlujoRepository repository;

    public List<Flujo> getByPoliticaId(String politicaId) {
        return repository.findByPoliticaId(politicaId);
    }

    public List<Flujo> getByActividadId(String actividadId) {
        return repository.findByActividadId(actividadId);
    }

    public Flujo create(Flujo flujo) {
        flujo.setCreatedAt(LocalDateTime.now());
        return repository.save(flujo);
    }

    public Flujo update(String id, Flujo flujo) {
        return repository.findById(id)
                .filter(f -> f.getDeletedAt() == null)
                .map(existing -> {
                    existing.setActividadId(flujo.getActividadId());
                    existing.setProceso(flujo.getProceso());
                    existing.setUpdatedAt(LocalDateTime.now());
                    return repository.save(existing);
                })
                .orElse(null);
    }

    public Flujo softDelete(String id) {
        return repository.findById(id)
                .filter(f -> f.getDeletedAt() == null)
                .map(existing -> {
                    existing.setDeletedAt(LocalDateTime.now());
                    return repository.save(existing);
                })
                .orElse(null);
    }
}
