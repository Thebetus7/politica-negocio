package com.example.politica_negocio.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class GeminiEvaluateRequest {
    private String tipo;
    private String nombre;
    private String condicion;
    private Map<String, Object> contexto;
}
