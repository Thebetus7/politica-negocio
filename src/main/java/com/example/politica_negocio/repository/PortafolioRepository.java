package com.example.politica_negocio.repository;

import com.example.politica_negocio.model.Portafolio;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface PortafolioRepository extends MongoRepository<Portafolio, String> {

    @Query("{ 'deletedAt' : null }")
    List<Portafolio> findAllActive();
}
