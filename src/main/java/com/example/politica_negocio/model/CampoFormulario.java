package com.example.politica_negocio.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CampoFormulario {
    private String id;
    private String tipo;
    private String etiqueta;
    private String placeholder;
    private Boolean requerido = false;
    private List<String> opciones = new ArrayList<>();
    private Integer orden;
    private String accept;
    private Integer tamanoMaxMb;
}
