package com.example.politica_negocio.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class LogDiagrama {

    private LocalDateTime tiempo;

    private String json;
}
