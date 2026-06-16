package com.example.politica_negocio.dto;

import lombok.Data;

@Data
public class GeminiEvaluateResponse {
    private String tipo;
    private String decision;
    private Boolean cumple;
    private String razon;
}
