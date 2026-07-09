package com.example.politica_negocio.repository;

import com.example.politica_negocio.model.Departamento;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;


public interface DepartamentoRepository extends MongoRepository<Departamento, String> {

    @Query("{ 'deletedAt' : null }")
    List<Departamento> findAllActive();
}
