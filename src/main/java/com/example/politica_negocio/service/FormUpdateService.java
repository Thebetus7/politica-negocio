package com.example.politica_negocio.service;

import com.example.politica_negocio.model.FormUpdate;
import com.example.politica_negocio.repository.FormUpdateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FormUpdateService {

    private final FormUpdateRepository repository;

    public List<FormUpdate> getByActividadId(String actividadId) {
        return repository.findByActividadId(actividadId);
    }

    public FormUpdate create(FormUpdate formUpdate) {
        formUpdate.setCreatedAt(LocalDateTime.now());
        return repository.save(formUpdate);
    }

    public FormUpdate update(String id, FormUpdate formUpdate) {
        return repository.findById(id)
                .filter(f -> f.getDeletedAt() == null)
                .map(existing -> {
                    existing.setContenidoUpdate(formUpdate.getContenidoUpdate());
                    existing.setUpdatedAt(LocalDateTime.now());
                    return repository.save(existing);
                })
                .orElse(null);
    }
}
