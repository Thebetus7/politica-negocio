package com.example.politica_negocio.service;

import com.example.politica_negocio.dto.GeminiEvaluateRequest;
import com.example.politica_negocio.dto.GeminiEvaluateResponse;
import com.example.politica_negocio.dto.GeminiPromptRequest;
import com.example.politica_negocio.dto.GeminiPromptResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
@Slf4j
public class GeminiAiClient {

    private final RestClient restClient;

    public GeminiAiClient(@Value("${fastapi.ai.base-url}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl)
                .build();
    }

    /** Llamada genérica gemini_api(prompt) vía microservicio FastAPI. */
    public String geminiApi(String prompt) {
        try {
            GeminiPromptResponse response = restClient.post()
                    .uri("/api/v1/gemini/prompt")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new GeminiPromptRequest(prompt))
                    .retrieve()
                    .body(GeminiPromptResponse.class);
            return response != null && response.getText() != null ? response.getText() : "";
        } catch (RestClientException e) {
            log.error("Error llamando FastAPI gemini/prompt: {}", e.getMessage());
            throw e;
        }
    }

    public GeminiEvaluateResponse evaluate(GeminiEvaluateRequest request) {
        try {
            GeminiEvaluateResponse response = restClient.post()
                    .uri("/api/v1/gemini/evaluate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(GeminiEvaluateResponse.class);
            if (response == null) {
                throw new RestClientException("Respuesta vacía de FastAPI gemini/evaluate");
            }
            return response;
        } catch (RestClientException e) {
            log.error("Error llamando FastAPI gemini/evaluate: {}", e.getMessage());
            throw e;
        }
    }
}
