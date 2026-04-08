package com.example.politica_negocio.repository;

import com.example.politica_negocio.model.PoliticaNegocio;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PoliticaNegocioRepository extends MongoRepository<PoliticaNegocio, String> {
    List<PoliticaNegocio> findByEstado(String estado);
}
