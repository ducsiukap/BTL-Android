package com.example.app_be.core.exception;

import org.springframework.http.HttpStatus;

public class EncodeException extends AppException {
    public EncodeException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public EncodeException() {
        super("Encoder error!", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
