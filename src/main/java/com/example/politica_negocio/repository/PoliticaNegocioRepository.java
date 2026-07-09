package com.example.politica_negocio.repository;

import com.example.politica_negocio.model.PoliticaNegocio;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import java.util.List;

public interface PoliticaNegocioRepository extends MongoRepository<PoliticaNegocio, String> {

    @Query("{ 'deletedAt': null }")
    List<PoliticaNegocio> findAllActive();
}
