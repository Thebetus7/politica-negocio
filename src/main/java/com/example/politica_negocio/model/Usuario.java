package com.example.politica_negocio.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "\"Usuario\"")
@Data
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Campos dummy para prueba de login rapido. La bd.md no los incluye pero se asume nombre o email
    @Column(name = "username",  columnDefinition = "TEXT")
    private String username;

    @Column(name = "password",  columnDefinition = "TEXT")
    private String password;
}
