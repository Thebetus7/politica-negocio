package com.example.politica_negocio.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@EqualsAndHashCode(callSuper = true)
@Document(collection = "versiones_documentos")
@Data
public class VersionDocumento extends BaseEntity {

    @Id
    private String id;

    private String documentoId;
    private int version;
    private String objectKey;
    private String nombreOriginal;
    private String comentario;
    private String contenidoTexto;
    private String modificadoPor;
}
