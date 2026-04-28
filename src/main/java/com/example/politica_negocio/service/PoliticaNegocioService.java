package com.example.politica_negocio.service;

import com.example.politica_negocio.model.PoliticaNegocio;
import com.example.politica_negocio.repository.PoliticaNegocioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PoliticaNegocioService {

    private final PoliticaNegocioRepository repository;

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
        return repository.save(politica);
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
}
