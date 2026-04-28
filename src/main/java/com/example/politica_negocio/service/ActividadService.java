package com.example.politica_negocio.service;

import com.example.politica_negocio.model.Actividad;
import com.example.politica_negocio.repository.ActividadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ActividadService {

    private final ActividadRepository repository;

    public List<Actividad> getByPoliticaId(String politicaId) {
        return repository.findByPoliticaIdAndDeletedAtIsNull(politicaId);
    }

    public Actividad getById(String id) {
        return repository.findById(id)
                .filter(a -> a.getDeletedAt() == null)
                .orElse(null);
    }

    public Actividad create(Actividad actividad) {
        actividad.setCreatedAt(LocalDateTime.now());
        return repository.save(actividad);
    }

    public Actividad update(String id, Actividad actividad) {
        return repository.findById(id)
                .filter(a -> a.getDeletedAt() == null)
                .map(existing -> {
                    existing.setNombre(actividad.getNombre());
                    existing.setEstado(actividad.getEstado());
                    existing.setDepartamentoId(actividad.getDepartamentoId());
                    existing.setEjeX(actividad.getEjeX());
                    existing.setEjeY(actividad.getEjeY());
                    existing.setTipoNodo(actividad.getTipoNodo());
                    existing.setFormUpdateId(actividad.getFormUpdateId());
                    existing.setActividadRefId(actividad.getActividadRefId());
                    existing.setUpdatedAt(LocalDateTime.now());
                    return repository.save(existing);
                })
                .orElse(null);
    }

    public Actividad softDelete(String id) {
        return repository.findById(id)
                .filter(a -> a.getDeletedAt() == null)
                .map(existing -> {
                    existing.setDeletedAt(LocalDateTime.now());
                    return repository.save(existing);
                })
                .orElse(null);
    }
}
