package com.example.politica_negocio.repository;

import com.example.politica_negocio.model.Departamento;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepartamentoRepository extends MongoRepository<Departamento, String> {

    @Query("{ 'deletedAt' : null }")
    List<Departamento> findAllActive();
}
