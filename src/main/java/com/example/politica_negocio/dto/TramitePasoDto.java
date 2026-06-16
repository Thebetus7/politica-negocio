package com.example.politica_negocio.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TramitePasoDto {
    private String actividadId;
    private String nombre;
    private String tipoNodo;
    private String estado;
    private String formularioId;
    private String contenidoUpdate;
    private LocalDateTime updatedAt;
    /** Para nodos decision: Sí / No elegido por IA */
    private String decisionTomada;
    /** activa | tomada | no_tomada */
    private String ramaVisual;
}
