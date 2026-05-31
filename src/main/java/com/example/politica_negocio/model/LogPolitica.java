package com.example.politica_negocio.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Document(collection = "logPoliticas")
@Data
public class LogPolitica extends BaseEntity {

    @Id
    private String id;

    private String politicaId;

    /** Versión incremental por cada compilación válida guardada */
    private int version;

    private LocalDateTime tiempo;

    /** true cuando la compilación pasó validación inicio→fin */
    private boolean valido;

    /**
     * false cuando el flujo dejó de ser válido (ej. se eliminó el nodo fin)
     * o fue reemplazado por una versión más nueva.
     */
    private boolean funcional = true;

    /** Snapshot del flujo compilado (nodos, conexiones, departamentos, formularios) */
    private Map<String, Object> flujoJson;

    /** Mensaje cuando valido=false o al invalidar */
    private String mensajeValidacion;
}
