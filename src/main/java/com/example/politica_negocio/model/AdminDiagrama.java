package com.example.politica_negocio.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@EqualsAndHashCode(callSuper = true)
@Document(collection = "adminDiagramas")
@Data
public class AdminDiagrama extends BaseEntity {

    @Id
    private String id;

    private String politicaId;

    private String logDiagramaId;

    private String userId;

    private String portafolioId;
}
