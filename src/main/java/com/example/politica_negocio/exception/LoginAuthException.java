package com.example.politica_negocio.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class LoginAuthException extends RuntimeException {

    private final HttpStatus status;
    private final String code;

    public LoginAuthException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }
}
