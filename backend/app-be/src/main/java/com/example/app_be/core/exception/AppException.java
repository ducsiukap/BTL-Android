package com.example.app_be.core.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.List;

@Getter
public abstract class AppException extends RuntimeException {
    private final Object errors;
    private final HttpStatus status;

    public AppException(String message, HttpStatus status, Object errors) {
        super(message);
        this.status = status;
        this.errors = errors;
    }

    public AppException(String message, HttpStatus status) {
        super(message);
        this.status = status;
        this.errors = null;
    }
}
