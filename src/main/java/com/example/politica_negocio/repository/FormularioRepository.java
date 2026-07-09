package com.example.politica_negocio.repository;

import com.example.politica_negocio.model.Formulario;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface FormularioRepository extends MongoRepository<Formulario, String> {

    @Query("{ 'deletedAt' : null }")
    List<Formulario> findAllActive();
}
