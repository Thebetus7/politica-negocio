package com.example.politica_negocio.service;

import com.example.politica_negocio.model.Departamento;
import com.example.politica_negocio.repository.DepartamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartamentoService {

    private final DepartamentoRepository repository;

    public List<Departamento> getAll() {
        return repository.findAllActive();
    }

    public Departamento getById(String id) {
        return repository.findById(id).filter(d -> d.getDeletedAt() == null).orElse(null);
    }

    public Departamento create(Departamento departamento) {
        departamento.setCreatedAt(LocalDateTime.now());
        return repository.save(departamento);
    }
    
    public Departamento update(String id, Departamento data) {
        Departamento existing = getById(id);
        if (existing != null) {
            existing.setNombre(data.getNombre());
            existing.setDescripcion(data.getDescripcion());
            existing.setUpdatedAt(LocalDateTime.now());
            return repository.save(existing);
        }
        return null;
    }

    public Departamento softDelete(String id) {
        Departamento departamento = getById(id);
        if (departamento != null) {
            departamento.setDeletedAt(LocalDateTime.now());
            return repository.save(departamento);
        }
        return null;
    }
}
