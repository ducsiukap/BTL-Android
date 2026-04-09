package com.example.app_be.controller.dto.response;

import com.example.app_be.model.User;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

public record UserResponseDto(
        UUID id,
        String fullName,
        String email,
        String role,
        Instant createdAt,
        Instant updatedAt
) {
    public static UserResponseDto from(User user) {
        return new UserResponseDto(
                user.getId(), user.getFullName(),
                user.getEmail(), user.getRole().name(),
                user.getCreatedAt(), user.getUpdatedAt()
        );
    }
}