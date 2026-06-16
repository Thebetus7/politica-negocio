package com.example.politica_negocio.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DocumentoResponseDto {
    private String id;
    private String nombreOriginal;
    private String mimeType;
    private Long size;
    private String downloadUrl;
}
