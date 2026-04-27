package com.example.politica_negocio.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Document(collection = "politicasNegocio")
@Data
public class PoliticaNegocio extends BaseEntity {

    @Id
    private String id;

    @NotBlank(message = "Nombre no puede estar vacío")
    private String nombre;

    private String descripcion;

    @NotBlank(message = "Estado no puede estar vacío")
    private String estado;
    
    private List<LogDiagrama> logs = new ArrayList<>();
}
