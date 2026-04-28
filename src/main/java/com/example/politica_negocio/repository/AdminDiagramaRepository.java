package com.example.politica_negocio.repository;

import com.example.politica_negocio.model.AdminDiagrama;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdminDiagramaRepository extends MongoRepository<AdminDiagrama, String> {

    @Query("{ 'politicaId': ?0, 'deletedAt': null }")
    List<AdminDiagrama> findByPoliticaId(String politicaId);
}
