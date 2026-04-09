package com.example.app_be.core.exception;

import org.springframework.http.HttpStatus;

public class InvalidCredentialsException extends AppException {
    public InvalidCredentialsException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }

    public InvalidCredentialsException() {
        super("Invalid credentials!", HttpStatus.UNAUTHORIZED);
    }
}
