package com.example.politica_negocio.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@EqualsAndHashCode(callSuper = true)
@Document(collection = "formUpdates")
@Data
public class FormUpdate extends BaseEntity {

    @Id
    private String id;

    /**
     * JSON con los datos llenados por el funcionario.
     * Contiene los valores ingresados para cada campo del formulario plantilla.
     */
    private String contenidoUpdate;

    /** Referencia al Formulario plantilla (estructura de campos) */
    private String formularioId;

    /** Referencia a la Actividad donde se usa este formulario */
    private String actividadId;
}
