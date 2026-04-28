package com.example.politica_negocio.service;

import com.example.politica_negocio.model.FuncionarioDepa;
import com.example.politica_negocio.repository.FuncionarioDepaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FuncionarioDepaService {

    private final FuncionarioDepaRepository repository;

    public List<FuncionarioDepa> getByDepartamentoId(String departamentoId) {
        return repository.findByDepartamentoId(departamentoId);
    }

    public List<FuncionarioDepa> getByUserId(String userId) {
        return repository.findByUserId(userId);
    }

    public FuncionarioDepa create(FuncionarioDepa funcionarioDepa) {
        funcionarioDepa.setCreatedAt(LocalDateTime.now());
        return repository.save(funcionarioDepa);
    }

    public FuncionarioDepa softDelete(String id) {
        return repository.findById(id)
                .filter(f -> f.getDeletedAt() == null)
                .map(existing -> {
                    existing.setDeletedAt(LocalDateTime.now());
                    return repository.save(existing);
                })
                .orElse(null);
    }
}
