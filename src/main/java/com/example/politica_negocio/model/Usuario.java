package com.example.politica_negocio.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;

@Document(collection = "usuarios")
@Data
public class Usuario {

    @Id
    private String id;

    @NotBlank(message = "Username no puede estar vacío")
    private String username;

    @NotBlank(message = "Password no puede estar vacío")
    private String password;
}
