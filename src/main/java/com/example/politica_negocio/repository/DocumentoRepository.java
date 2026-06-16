package com.example.politica_negocio.repository;

import com.example.politica_negocio.model.Documento;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface DocumentoRepository extends MongoRepository<Documento, String> {

    Optional<Documento> findByIdAndDeletedAtIsNull(String id);

    List<Documento> findByPortafolioIdAndDeletedAtIsNull(String portafolioId);
}
