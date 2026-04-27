package com.example.politica_negocio.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@EqualsAndHashCode(callSuper = true)
@Document(collection = "departamentos")
@Data
public class Departamento extends BaseEntity {

    @Id
    private String id;
    
    private String nombre;
    
    private String descripcion;
}
