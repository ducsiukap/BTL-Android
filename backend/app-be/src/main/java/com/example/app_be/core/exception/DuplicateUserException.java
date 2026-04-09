package com.example.app_be.core.exception;

import org.springframework.http.HttpStatus;

public class DuplicateUserException extends AppException {
    public DuplicateUserException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
