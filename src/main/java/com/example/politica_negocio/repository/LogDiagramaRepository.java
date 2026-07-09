package com.example.politica_negocio.repository;

import com.example.politica_negocio.model.LogDiagrama;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface LogDiagramaRepository extends MongoRepository<LogDiagrama, String> {
}
