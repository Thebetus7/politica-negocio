package com.example.politica_negocio.repository;

import com.example.politica_negocio.model.LogPolitica;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LogPoliticaRepository extends MongoRepository<LogPolitica, String> {

    @Query("{ 'politicaId': ?0, 'deletedAt': null }")
    List<LogPolitica> findByPoliticaIdAndDeletedAtIsNullOrderByVersionDesc(String politicaId);

    @Query(value = "{ 'politicaId': ?0, 'valido': true, 'funcional': true, 'deletedAt': null }", sort = "{ 'version': -1 }")
    Optional<LogPolitica> findFirstByPoliticaIdAndValidoTrueAndFuncionalTrueAndDeletedAtIsNull(String politicaId);

    @Query("{ 'politicaId': ?0, 'funcional': true, 'deletedAt': null }")
    List<LogPolitica> findByPoliticaIdAndFuncionalTrueAndDeletedAtIsNull(String politicaId);
}
