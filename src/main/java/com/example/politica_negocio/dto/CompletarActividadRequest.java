package com.example.politica_negocio.dto;

import lombok.Data;

@Data
public class CompletarActividadRequest {
    private String formularioId;
    private String contenidoUpdate;
    /** Para nodos decisión: "Sí" / "No" (o label de la rama) */
    private String decisionLabel;
    /** Para nodos pregunta iterativa: true = repetir, false = salir del ciclo */
    private Boolean continuarIteracion;
}
