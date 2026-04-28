package com.example.politica_negocio.repository;

import com.example.politica_negocio.model.FuncionarioDepa;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FuncionarioDepaRepository extends MongoRepository<FuncionarioDepa, String> {

    @Query("{ 'departamentoId': ?0, 'deletedAt': null }")
    List<FuncionarioDepa> findByDepartamentoId(String departamentoId);

    @Query("{ 'userId': ?0, 'deletedAt': null }")
    List<FuncionarioDepa> findByUserId(String userId);
}
