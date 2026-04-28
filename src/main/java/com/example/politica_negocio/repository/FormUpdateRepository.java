package com.example.politica_negocio.repository;

import com.example.politica_negocio.model.FormUpdate;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FormUpdateRepository extends MongoRepository<FormUpdate, String> {

    @Query("{ 'actividadId': ?0, 'deletedAt': null }")
    List<FormUpdate> findByActividadId(String actividadId);

    @Query("{ 'formularioId': ?0, 'deletedAt': null }")
    List<FormUpdate> findByFormularioId(String formularioId);
}
