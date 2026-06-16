package com.example.politica_negocio.service;

import com.example.politica_negocio.dto.DocumentoResponseDto;
import com.example.politica_negocio.model.Documento;
import com.example.politica_negocio.repository.DocumentoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentoService {

    private final S3Client s3Client;
    private final DocumentoRepository documentoRepository;

    @Value("${storage.s3.bucket}")
    private String bucket;

    @Value("${storage.s3.endpoint}")
    private String endpoint;

    @Value("${storage.s3.region}")
    private String region;

    @Value("${storage.s3.access-key}")
    private String accessKey;

    @Value("${storage.s3.secret-key}")
    private String secretKey;

    public DocumentoResponseDto subir(
            MultipartFile file,
            String actividadId,
            String portafolioId,
            String subidoPor) {

        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Archivo vacío");
        }

        String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "documento";
        String objectKey = generarObjectKey(originalName);

        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .contentType(file.getContentType())
                    .build();
            s3Client.putObject(putRequest, RequestBody.fromBytes(file.getBytes()));
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error leyendo archivo");
        } catch (Exception e) {
            log.error("Error subiendo a S3/MinIO: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "No se pudo almacenar el documento");
        }

        Documento doc = new Documento();
        doc.setNombreOriginal(originalName);
        doc.setMimeType(file.getContentType());
        doc.setSize(file.getSize());
        doc.setBucket(bucket);
        doc.setObjectKey(objectKey);
        doc.setActividadId(actividadId);
        doc.setPortafolioId(portafolioId);
        doc.setSubidoPor(subidoPor);
        doc.setCreatedAt(LocalDateTime.now());
        Documento saved = documentoRepository.save(doc);

        return toDto(saved);
    }

    public String presignedDownloadUrl(String documentoId) {
        Documento doc = requireDocumento(documentoId);
        return presignUrl(doc.getBucket(), doc.getObjectKey());
    }

    public DocumentoResponseDto getDto(String documentoId) {
        return toDto(requireDocumento(documentoId));
    }

    public Optional<Documento> findById(String id) {
        return documentoRepository.findByIdAndDeletedAtIsNull(id);
    }

    public void vincularFormUpdate(String documentoId, String formUpdateId) {
        documentoRepository.findByIdAndDeletedAtIsNull(documentoId).ifPresent(doc -> {
            doc.setFormUpdateId(formUpdateId);
            doc.setUpdatedAt(LocalDateTime.now());
            documentoRepository.save(doc);
        });
    }

    public boolean softDelete(String id) {
        return documentoRepository.findByIdAndDeletedAtIsNull(id).map(doc -> {
            doc.setDeletedAt(LocalDateTime.now());
            documentoRepository.save(doc);
            return true;
        }).orElse(false);
    }

    private Documento requireDocumento(String id) {
        return documentoRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Documento no encontrado"));
    }

    private String generarObjectKey(String originalName) {
        String safe = originalName.replaceAll("[^a-zA-Z0-9._-]", "_");
        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        return fecha + "/" + UUID.randomUUID() + "-" + safe;
    }

    private String presignUrl(String bucketName, String objectKey) {
        try (S3Presigner presigner = S3Presigner.builder()
                .endpointOverride(java.net.URI.create(endpoint))
                .region(software.amazon.awssdk.regions.Region.of(region))
                .credentialsProvider(software.amazon.awssdk.auth.credentials.StaticCredentialsProvider.create(
                        software.amazon.awssdk.auth.credentials.AwsBasicCredentials.create(accessKey, secretKey)))
                .serviceConfiguration(software.amazon.awssdk.services.s3.S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .build())
                .build()) {

            GetObjectRequest getRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();
            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(15))
                    .getObjectRequest(getRequest)
                    .build();
            PresignedGetObjectRequest presigned = presigner.presignGetObject(presignRequest);
            return presigned.url().toString();
        }
    }

    private DocumentoResponseDto toDto(Documento doc) {
        return DocumentoResponseDto.builder()
                .id(doc.getId())
                .nombreOriginal(doc.getNombreOriginal())
                .mimeType(doc.getMimeType())
                .size(doc.getSize())
                .downloadUrl(presignUrl(doc.getBucket(), doc.getObjectKey()))
                .build();
    }
}
