package com.example.politica_negocio.repository;

import com.example.politica_negocio.model.Usuario;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends MongoRepository<Usuario, String> {

    @Query("{ 'correo' : ?0, 'deletedAt' : null }")
    Optional<Usuario> findByCorreo(String correo);

    @Query("{ 'deletedAt' : null }")
    List<Usuario> findAllActive();
}
