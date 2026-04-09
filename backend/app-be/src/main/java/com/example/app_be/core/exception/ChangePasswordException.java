package com.example.app_be.core.exception;

import org.springframework.http.HttpStatus;

public class ChangePasswordException extends AppException {
    public ChangePasswordException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
