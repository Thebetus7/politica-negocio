package com.example.politica_negocio.repository;

import com.example.politica_negocio.model.FuncionarioDepa;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import java.util.List;

public interface FuncionarioDepaRepository extends MongoRepository<FuncionarioDepa, String> {

    @Query("{ 'departamentoId': ?0, 'deletedAt': null }")
    List<FuncionarioDepa> findByDepartamentoId(String departamentoId);

    @Query("{ 'userId': ?0, 'deletedAt': null }")
    List<FuncionarioDepa> findByUserId(String userId);
}
