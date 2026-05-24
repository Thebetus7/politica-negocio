package com.example.politica_negocio.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@EqualsAndHashCode(callSuper = true)
@Document(collection = "portafolios")
@Data
public class Portafolio extends BaseEntity {

    @Id
    private String id;

    /**
     * JSON en formato String que guarda solo texto:
     * carnet, nota, descripcion y otros datos.
     * Sin imágenes.
     */
    private String json;

    private String politicaId;
    
    private String creadorId;
    
    private String estado;
}
