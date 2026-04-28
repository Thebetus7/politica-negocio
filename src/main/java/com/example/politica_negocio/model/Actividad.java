package com.example.politica_negocio.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@EqualsAndHashCode(callSuper = true)
@Document(collection = "actividades")
@Data
public class Actividad extends BaseEntity {

    @Id
    private String id;

    private String politicaId;

    private String departamentoId;

    /** Referencia a otra actividad (para relaciones internas) */
    private String actividadRefId;

    /** Referencia al FormUpdate asociado (opcional) */
    private String formUpdateId;

    private String nombre;

    private String estado;

    /** Posición X del nodo en el canvas del editor */
    private String ejeX;

    /** Posición Y del nodo en el canvas del editor */
    private String ejeY;

    /** Tipo de nodo: actividad, decision, while_do, do_while, fork, join, inicio, fin */
    private String tipoNodo;
}
