package com.example.politica_negocio.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalTime;

@Entity
@Table(name = "\"Log Diagrama\"")
@Data
public class LogDiagrama {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "\"JSON\"", columnDefinition = "TEXT")
    private String json;

    @Column(name = "\"tiempo\"")
    private LocalTime tiempo;
}
