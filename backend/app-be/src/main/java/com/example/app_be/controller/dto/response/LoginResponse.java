package com.example.app_be.controller.dto.response;

import com.example.app_be.model.UserRole;

import java.util.UUID;

public record LoginResponse(
        UUID id,
        String fullName,
        String role,
        String accessToken,
        String refreshToken
) {
}