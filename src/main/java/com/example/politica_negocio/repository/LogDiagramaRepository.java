package com.example.politica_negocio.repository;

import com.example.politica_negocio.model.LogDiagrama;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogDiagramaRepository extends MongoRepository<LogDiagrama, String> {
}
