package com.example.politica_negocio.repository;

import com.example.politica_negocio.model.Flujo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FlujoRepository extends MongoRepository<Flujo, String> {

    @Query("{ 'politicaId': ?0, 'deletedAt': null }")
    List<Flujo> findByPoliticaId(String politicaId);

    @Query("{ 'actividadId': ?0, 'deletedAt': null }")
    List<Flujo> findByActividadId(String actividadId);
}
