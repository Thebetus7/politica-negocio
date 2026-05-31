package com.example.politica_negocio.repository;

import com.example.politica_negocio.model.Flujo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FlujoRepository extends MongoRepository<Flujo, String> {

    @Query("{ 'politicaId': ?0, 'portafolioId': null, 'deletedAt': null }")
    List<Flujo> findPlantillasByPoliticaId(String politicaId);

    @Query("{ 'politicaId': ?0, 'portafolioId': ?1, 'deletedAt': null }")
    List<Flujo> findInstanciasByPoliticaIdAndPortafolioId(String politicaId, String portafolioId);

    @Query("{ 'actividadId': ?0, 'deletedAt': null }")
    List<Flujo> findByActividadId(String actividadId);

    @Query("{ '$or': [ { 'actividadId': ?0 }, { 'proceso.siguientes.actividadDestinoId': ?0 } ], 'deletedAt': null }")
    List<Flujo> findActiveByActividadAsSourceOrTarget(String actividadId);
}
