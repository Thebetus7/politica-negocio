package com.example.politica_negocio.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@EqualsAndHashCode(callSuper = true)
@Document(collection = "documentos")
@Data
public class Documento extends BaseEntity {

    @Id
    private String id;

    private String nombreOriginal;
    private String mimeType;
    private Long size;
    private String bucket;
    private String objectKey;

    private String actividadId;
    private String portafolioId;
    private String formUpdateId;
    private String subidoPor;

    private String estado = "PENDIENTE";
    private int versionActual = 1;
    private String contenidoTexto;
    private String politicaId;
}
