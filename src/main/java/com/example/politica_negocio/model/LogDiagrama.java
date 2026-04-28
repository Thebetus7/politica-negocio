package com.example.politica_negocio.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Document(collection = "logDiagramas")
@Data
public class LogDiagrama extends BaseEntity {

    @Id
    private String id;

    private LocalDateTime tiempo;

    /**
     * JSON del snapshot completo del diagrama.
     * Contiene toda la estructura de nodos, posiciones y conexiones
     * al momento de guardar.
     */
    private Map<String, Object> json;
}
