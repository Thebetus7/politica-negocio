package com.example.politica_negocio.service;

import com.example.politica_negocio.model.Portafolio;
import com.example.politica_negocio.repository.PortafolioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PortafolioService {

    private final PortafolioRepository repository;

    public List<Portafolio> getAll() {
        return repository.findAllActive();
    }

    public Portafolio getById(String id) {
        return repository.findById(id).filter(p -> p.getDeletedAt() == null).orElse(null);
    }

    public Portafolio create(Portafolio portafolio) {
        portafolio.setCreatedAt(LocalDateTime.now());
        return repository.save(portafolio);
    }
}
