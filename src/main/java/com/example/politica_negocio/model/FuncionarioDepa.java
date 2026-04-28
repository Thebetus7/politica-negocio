package com.example.politica_negocio.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@EqualsAndHashCode(callSuper = true)
@Document(collection = "funcionariosDepa")
@Data
public class FuncionarioDepa extends BaseEntity {

    @Id
    private String id;

    /** ID del usuario con rol FUNCIONARIO */
    private String userId;

    /** ID del departamento asignado */
    private String departamentoId;
}
