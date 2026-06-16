package com.example.politica_negocio.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TramiteProgresoDto {
    private String portafolioId;
    private String politicaId;
    private String politicaNombre;
    private String creadorId;
    private String estado;
    private double progreso;
    private List<TramitePasoDto> pasos;
}
