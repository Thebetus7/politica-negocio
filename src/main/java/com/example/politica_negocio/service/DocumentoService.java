package com.example.politica_negocio.service;

import com.example.politica_negocio.dto.DocumentoResponseDto;
import com.example.politica_negocio.model.Documento;
import com.example.politica_negocio.model.VersionDocumento;
import com.example.politica_negocio.model.Actividad;
import com.example.politica_negocio.repository.DocumentoRepository;
import com.example.politica_negocio.repository.VersionDocumentoRepository;
import com.example.politica_negocio.repository.ActividadRepository;
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
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFPicture;
import org.apache.poi.xwpf.usermodel.XWPFPictureData;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentoService {

    private final S3Client s3Client;
    private final DocumentoRepository documentoRepository;
    private final VersionDocumentoRepository versionDocumentoRepository;
    private final ActividadRepository actividadRepository;

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

    private String extraerContenidoTexto(MultipartFile file, String filename) {
        if (file == null || file.isEmpty()) {
            return "";
        }
        String ext = getExtension(filename).toLowerCase();
        try {
            if (ext.equals("txt")) {
                String raw = new String(file.getBytes(), java.nio.charset.StandardCharsets.UTF_8);
                return "<p>" + raw.replace("&", "&amp;")
                                  .replace("<", "&lt;")
                                  .replace(">", "&gt;")
                                  .replace("\n", "</p><p>") + "</p>";
            } else if (ext.equals("pdf")) {
                try (org.apache.pdfbox.pdmodel.PDDocument pdfDoc = org.apache.pdfbox.pdmodel.PDDocument.load(file.getInputStream())) {
                    org.apache.pdfbox.text.PDFTextStripper stripper = new org.apache.pdfbox.text.PDFTextStripper();
                    String rawText = stripper.getText(pdfDoc);
                    return "<p>" + rawText.replace("&", "&amp;")
                                          .replace("<", "&lt;")
                                          .replace(">", "&gt;")
                                          .replace("\n", "</p><p>") + "</p>";
                }
            } else if (ext.equals("docx")) {
                try (XWPFDocument docxDoc = new XWPFDocument(file.getInputStream())) {
                    StringBuilder sb = new StringBuilder();
                    for (XWPFParagraph paragraph : docxDoc.getParagraphs()) {
                        StringBuilder paragraphHtml = new StringBuilder();
                        for (XWPFRun run : paragraph.getRuns()) {
                            // 1. Extraer imágenes incrustadas en el Run
                            List<XWPFPicture> pictures = run.getEmbeddedPictures();
                            if (pictures != null && !pictures.isEmpty()) {
                                for (XWPFPicture picture : pictures) {
                                    XWPFPictureData pictureData = picture.getPictureData();
                                    byte[] pictureBytes = pictureData.getData();
                                     String imgFilename = pictureData.getFileName();
                                     String imgMime = "image/png";
                                     if (imgFilename != null) {
                                         String imgFilenameLower = imgFilename.toLowerCase();
                                         if (imgFilenameLower.endsWith(".jpg") || imgFilenameLower.endsWith(".jpeg")) {
                                             imgMime = "image/jpeg";
                                         } else if (imgFilenameLower.endsWith(".gif")) {
                                             imgMime = "image/gif";
                                         }
                                     }
                                    
                                    String imageKey = "documentos/imagenes/" + UUID.randomUUID() + "-" + imgFilename;
                                    try {
                                        PutObjectRequest putRequest = PutObjectRequest.builder()
                                                .bucket(bucket)
                                                .key(imageKey)
                                                .contentType(imgMime)
                                                .build();
                                        s3Client.putObject(putRequest, RequestBody.fromBytes(pictureBytes));
                                        
                                        String imageUrl = presignUrl(bucket, imageKey);
                                        paragraphHtml.append("<img src=\"").append(imageUrl).append("\" style=\"max-width:100%;\" />");
                                    } catch (Exception e) {
                                        log.error("Error subiendo imagen incrustada de Word a MinIO/S3: {}", e.getMessage());
                                    }
                                }
                            }
                            
                            // 2. Extraer texto con formato
                            String runText = run.getText(0);
                            if (runText != null && !runText.isEmpty()) {
                                StringBuilder runHtml = new StringBuilder();
                                String escapedText = runText.replace("&", "&amp;")
                                                            .replace("<", "&lt;")
                                                            .replace(">", "&gt;")
                                                            .replace("\n", "<br/>");
                                runHtml.append(escapedText);
                                
                                if (run.isBold()) {
                                    runHtml.insert(0, "<strong>").append("</strong>");
                                }
                                if (run.isItalic()) {
                                    runHtml.insert(0, "<em>").append("</em>");
                                }
                                 if (run.getUnderline() != org.apache.poi.xwpf.usermodel.UnderlinePatterns.NONE) {
                                    runHtml.insert(0, "<u>").append("</u>");
                                }
                                if (run.isStrikeThrough()) {
                                    runHtml.insert(0, "<s>").append("</s>");
                                }
                                paragraphHtml.append(runHtml);
                            }
                        }
                        
                        String style = paragraph.getStyle();
                        if (style != null && style.startsWith("Heading")) {
                            String level = style.replace("Heading", "");
                            paragraphHtml.insert(0, "<h" + level + ">").append("</h" + level + ">");
                        } else {
                            paragraphHtml.insert(0, "<p>").append("</p>");
                        }
                        sb.append(paragraphHtml);
                    }
                    return sb.toString();
                }
            } else if (ext.equals("xlsx")) {
                try (org.apache.poi.xssf.usermodel.XSSFWorkbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook(file.getInputStream())) {
                    StringBuilder sb = new StringBuilder();
                    org.apache.poi.ss.usermodel.DataFormatter formatter = new org.apache.poi.ss.usermodel.DataFormatter();
                    for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                        org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheetAt(i);
                        sb.append("<h3 class=\"text-indigo-650 mt-4 mb-2\">Hoja: ").append(sheet.getSheetName()).append("</h3>");
                        sb.append("<table border=\"1\" style=\"border-collapse:collapse; width:100%; font-size:12px; margin-bottom:16px; border:1px solid #e5e7eb;\">");
                        for (org.apache.poi.ss.usermodel.Row row : sheet) {
                            sb.append("<tr style=\"border-bottom:1px solid #e5e7eb;\">");
                            for (org.apache.poi.ss.usermodel.Cell cell : row) {
                                String cellText = formatter.formatCellValue(cell);
                                sb.append("<td style=\"padding:6px; border-right:1px solid #e5e7eb;\">")
                                  .append(cellText != null ? cellText.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;") : "")
                                  .append("</td>");
                            }
                            sb.append("</tr>");
                        }
                        sb.append("</table>");
                    }
                    return sb.toString();
                }
            }
        } catch (Exception e) {
            log.warn("No se pudo extraer texto enriquecido del archivo ({}): {}", filename, e.getMessage());
        }
        return "";
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

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

        String politicaId = null;
        if (actividadId != null && !actividadId.isEmpty()) {
            Optional<Actividad> actOpt = actividadRepository.findById(actividadId);
            if (actOpt.isPresent()) {
                politicaId = actOpt.get().getPoliticaId();
            }
        }

        String textoExtraido = extraerContenidoTexto(file, originalName);

        Documento doc = new Documento();
        doc.setNombreOriginal(originalName);
        doc.setMimeType(file.getContentType());
        doc.setSize(file.getSize());
        doc.setBucket(bucket);
        doc.setObjectKey(objectKey);
        doc.setActividadId(actividadId);
        doc.setPortafolioId(portafolioId);
        doc.setPoliticaId(politicaId);
        doc.setEstado("PENDIENTE");
        doc.setVersionActual(1);
        doc.setContenidoTexto(textoExtraido);
        doc.setSubidoPor(subidoPor);
        doc.setCreatedAt(LocalDateTime.now());
        Documento saved = documentoRepository.save(doc);

        // Crear la versión inicial 1 en el histórico
        VersionDocumento v1 = new VersionDocumento();
        v1.setDocumentoId(saved.getId());
        v1.setVersion(1);
        v1.setObjectKey(objectKey);
        v1.setNombreOriginal(originalName);
        v1.setComentario("Subida inicial del documento");
        v1.setContenidoTexto(textoExtraido);
        v1.setModificadoPor(subidoPor);
        v1.setCreatedAt(LocalDateTime.now());
        versionDocumentoRepository.save(v1);

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
        try {
            AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
            StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(credentials);
            Region r = Region.of(region);

            try (S3Presigner presigner = S3Presigner.builder()
                    .endpointOverride(URI.create(endpoint))
                    .region(r)
                    .credentialsProvider(credentialsProvider)
                    .serviceConfiguration(software.amazon.awssdk.services.s3.S3Configuration.builder()
                            .pathStyleAccessEnabled(true)
                            .build())
                    .build()) {

                GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                        .bucket(bucketName)
                        .key(objectKey)
                        .build();

                GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofMinutes(60))
                        .getObjectRequest(getObjectRequest)
                        .build();

                PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(getObjectPresignRequest);
                return presignedRequest.url().toString();
            }
        } catch (Exception e) {
            log.error("Error generando presigned URL: {}", e.getMessage());
            return "";
        }
    }

    public List<DocumentoResponseDto> listar(String busqueda, String estado, boolean ordenFechaDesc) {
        List<Documento> docs;
        if (ordenFechaDesc) {
            docs = documentoRepository.findByDeletedAtIsNullOrderByCreatedAtDesc();
        } else {
            docs = documentoRepository.findByDeletedAtIsNullOrderByCreatedAtAsc();
        }

        return docs.stream()
                .filter(doc -> {
                    if (estado != null && !estado.isEmpty()) {
                        return doc.getEstado().equalsIgnoreCase(estado);
                    }
                    return true;
                })
                .filter(doc -> {
                    if (busqueda != null && !busqueda.isEmpty()) {
                        String term = busqueda.toLowerCase();
                        return doc.getNombreOriginal().toLowerCase().contains(term) ||
                                (doc.getContenidoTexto() != null && doc.getContenidoTexto().toLowerCase().contains(term));
                    }
                    return true;
                })
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<DocumentoResponseDto> listarAceptadosPorPolitica(String politicaId) {
        return documentoRepository.findByPoliticaIdAndEstadoAndDeletedAtIsNullOrderByCreatedAtDesc(politicaId, "ACEPTADO")
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public DocumentoResponseDto cambiarEstado(String id, String nuevoEstado) {
        Documento doc = requireDocumento(id);
        doc.setEstado(nuevoEstado.toUpperCase());
        doc.setUpdatedAt(LocalDateTime.now());
        Documento saved = documentoRepository.save(doc);
        return toDto(saved);
    }

    public DocumentoResponseDto actualizarContenidoTexto(String id, String contenido) {
        Documento doc = requireDocumento(id);
        doc.setContenidoTexto(contenido);
        doc.setUpdatedAt(LocalDateTime.now());
        Documento saved = documentoRepository.save(doc);
        return toDto(saved);
    }

    public List<VersionDocumento> obtenerVersiones(String documentoId) {
        return versionDocumentoRepository.findByDocumentoIdAndDeletedAtIsNullOrderByVersionDesc(documentoId);
    }

    public VersionDocumento crearNuevaVersion(String id, String comentario, String modificadoPor) {
        Documento doc = requireDocumento(id);
        int nextVersion = doc.getVersionActual() + 1;
        String content = doc.getContenidoTexto() != null ? doc.getContenidoTexto() : "";
        byte[] bytes = content.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        String versionKey = "documentos/versiones/" + doc.getId() + "-v" + nextVersion + ".html";

        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(doc.getBucket())
                    .key(versionKey)
                    .contentType("text/html")
                    .build();
            s3Client.putObject(putRequest, RequestBody.fromBytes(bytes));
        } catch (Exception e) {
            log.error("Error subiendo versión a MinIO/S3: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "No se pudo almacenar la versión en el storage");
        }

        VersionDocumento v = new VersionDocumento();
        v.setDocumentoId(doc.getId());
        v.setVersion(nextVersion);
        v.setObjectKey(versionKey);
        v.setNombreOriginal(doc.getNombreOriginal());
        v.setComentario(comentario);
        v.setContenidoTexto(content);
        v.setModificadoPor(modificadoPor);
        v.setCreatedAt(LocalDateTime.now());
        VersionDocumento savedVersion = versionDocumentoRepository.save(v);

        doc.setVersionActual(nextVersion);
        doc.setObjectKey(versionKey);
        doc.setUpdatedAt(LocalDateTime.now());
        documentoRepository.save(doc);

        return savedVersion;
    }

    public DocumentoResponseDto revertirAVersion(String id, String versionId, String modificadoPor) {
        Documento doc = requireDocumento(id);
        VersionDocumento v = versionDocumentoRepository.findById(versionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Versión de documento no encontrada"));

        doc.setContenidoTexto(v.getContenidoTexto());

        int nextVersion = doc.getVersionActual() + 1;
        String content = v.getContenidoTexto() != null ? v.getContenidoTexto() : "";
        byte[] bytes = content.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        String versionKey = "documentos/versiones/" + doc.getId() + "-v" + nextVersion + ".html";

        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(doc.getBucket())
                    .key(versionKey)
                    .contentType("text/html")
                    .build();
            s3Client.putObject(putRequest, RequestBody.fromBytes(bytes));
        } catch (Exception e) {
            log.error("Error subiendo versión de reversión a MinIO/S3: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "No se pudo almacenar la versión en el storage");
        }

        VersionDocumento rollbackVersion = new VersionDocumento();
        rollbackVersion.setDocumentoId(doc.getId());
        rollbackVersion.setVersion(nextVersion);
        rollbackVersion.setObjectKey(versionKey);
        rollbackVersion.setNombreOriginal(doc.getNombreOriginal() + " (v" + nextVersion + ")");
        rollbackVersion.setComentario("Revertido a la versión v" + v.getVersion() + ": " + v.getComentario());
        rollbackVersion.setContenidoTexto(content);
        rollbackVersion.setModificadoPor(modificadoPor);
        rollbackVersion.setCreatedAt(LocalDateTime.now());
        versionDocumentoRepository.save(rollbackVersion);

        doc.setVersionActual(nextVersion);
        doc.setObjectKey(versionKey);
        doc.setUpdatedAt(LocalDateTime.now());
        Documento saved = documentoRepository.save(doc);

        return toDto(saved);
    }

    private DocumentoResponseDto toDto(Documento doc) {
        return DocumentoResponseDto.builder()
                .id(doc.getId())
                .nombreOriginal(doc.getNombreOriginal())
                .mimeType(doc.getMimeType())
                .size(doc.getSize())
                .downloadUrl(presignUrl(doc.getBucket(), doc.getObjectKey()))
                .estado(doc.getEstado())
                .versionActual(doc.getVersionActual())
                .contenidoTexto(doc.getContenidoTexto())
                .politicaId(doc.getPoliticaId())
                .actividadId(doc.getActividadId())
                .portafolioId(doc.getPortafolioId())
                .build();
    }
}
