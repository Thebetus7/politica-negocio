package com.example.politica_negocio.service;

import com.example.politica_negocio.model.CampoFormulario;
import com.example.politica_negocio.model.Formulario;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.ParameterizedTypeReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class VozFormularioService {

    private final FormularioService formularioService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${fastapi.ai.base-url}")
    private String fastapiBaseUrl;

    public Map<String, Object> llenarDesdeAudio(String formularioId, MultipartFile audio) {
        Formulario formulario = formularioService.getById(formularioId);
        if (formulario == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Formulario no encontrado");
        }
        if (audio == null || audio.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Audio vacío");
        }

        List<Map<String, Object>> campos = new ArrayList<>();
        List<CampoFormulario> definicionCampos = formulario.getCampos() != null
                ? formulario.getCampos()
                : List.of();
        for (CampoFormulario campo : definicionCampos) {
            if (campo == null || campo.getTipo() == null) continue;
            String tipo = campo.getTipo();
            if ("archivo".equals(tipo) || "boton".equals(tipo)) continue;
            Map<String, Object> def = new java.util.LinkedHashMap<>();
            def.put("id", campo.getId());
            def.put("etiqueta", campo.getEtiqueta());
            def.put("tipo", tipo);
            def.put("requerido", campo.getRequerido());
            if (campo.getOpciones() != null && !campo.getOpciones().isEmpty()) {
                def.put("opciones", campo.getOpciones());
            }
            campos.add(def);
        }

        try {
            String camposJson = objectMapper.writeValueAsString(campos);
            String baseUrl = fastapiBaseUrl.endsWith("/") ? fastapiBaseUrl.substring(0, fastapiBaseUrl.length() - 1) : fastapiBaseUrl;

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("campos", camposJson);
            body.add("audio", new ByteArrayResource(audio.getBytes()) {
                @Override
                public String getFilename() {
                    return audio.getOriginalFilename() != null ? audio.getOriginalFilename() : "audio.webm";
                }
            });

            RestClient client = RestClient.builder().baseUrl(baseUrl).build();
            Map<String, Object> response = client.post()
                    .uri("/api/v1/voice/fill-form")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {});

            if (response == null) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Respuesta vacía del servicio de voz");
            }
            return response;
        } catch (RestClientException e) {
            log.error("Error llamando FastAPI voice: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "No se pudo procesar el audio: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error procesando audio: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno al procesar audio");
        }
    }
}
