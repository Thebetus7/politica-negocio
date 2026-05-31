package com.example.politica_negocio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogPoliticaCompileResult {
    private boolean valido;
    private Integer version;
    private String mensaje;
    private Map<String, Object> flujoJson;
}
