package com.example.politica_negocio.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TareaFuncionarioDto {
    private String portafolioId;
    private String politicaId;
    private String actividadId;
    private String actividadNombre;
    private String tipoNodo;
    private String departamentoId;
    private String formularioId;
    private String flujoInstanciaId;
    private String portafolioJson;
}
