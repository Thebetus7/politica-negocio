package com.example.politica_negocio.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Document(collection = "formularios")
@Data
public class Formulario extends BaseEntity {

    @Id
    private String id;

    @NotBlank(message = "El nombre del formulario no puede estar vacío")
    private String nombre;

    private String descripcion;

    /**
     * Estructura JSON del formulario.
     * Cada elemento es un Map con las propiedades del campo:
     * - tipo: "texto" | "texto_largo" | "numero" | "fecha" | "lista" | "checkbox" | "radio" | "archivo" | "email" | "telefono"
     * - etiqueta: nombre visible del campo
     * - placeholder: texto de ejemplo
     * - requerido: boolean
     * - opciones: List<String> (solo para lista y radio)
     * - orden: número de posición
     */
    private List<Map<String, Object>> campos = new ArrayList<>();
}
