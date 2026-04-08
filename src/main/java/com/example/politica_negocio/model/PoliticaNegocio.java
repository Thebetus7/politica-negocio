package com.example.politica_negocio.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "\"Política Negocio\"")
@Data
public class PoliticaNegocio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "\"nombre\"", columnDefinition = "TEXT")
    private String nombre;

    @Column(name = "\"descripcion\"", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "\"estado\"", columnDefinition = "TEXT")
    private String estado;
}
