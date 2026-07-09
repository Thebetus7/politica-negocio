package com.example.politica_negocio.repository;

import com.example.politica_negocio.model.Actividad;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface ActividadRepository extends MongoRepository<Actividad, String> {

    @Query("{ 'politicaId': ?0, 'deletedAt': null }")
    List<Actividad> findByPoliticaIdAndDeletedAtIsNull(String politicaId);
}
