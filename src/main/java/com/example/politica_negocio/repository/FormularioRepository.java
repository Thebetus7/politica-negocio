package com.example.politica_negocio.repository;

import com.example.politica_negocio.model.Formulario;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FormularioRepository extends MongoRepository<Formulario, String> {

    @Query("{ 'deletedAt' : null }")
    List<Formulario> findAllActive();
}
