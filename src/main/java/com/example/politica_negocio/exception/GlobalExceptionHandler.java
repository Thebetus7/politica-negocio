package com.example.politica_negocio.exception;

import com.example.politica_negocio.controller.dto.ApiErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(LoginAuthException.class)
    public ResponseEntity<ApiErrorResponse> handleLoginAuth(LoginAuthException ex) {
        return ResponseEntity
                .status(ex.getStatus())
                .body(ApiErrorResponse.builder()
                        .message(ex.getMessage())
                        .code(ex.getCode())
                        .build());
    }
}
