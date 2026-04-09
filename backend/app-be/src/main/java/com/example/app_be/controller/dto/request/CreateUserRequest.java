package com.example.app_be.controller.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank(message = "Full name is required")
        String fullName,

        @NotBlank(message = "Email is required")
        @Email(message = "Email is not valid")
        String email,

//        @NotBlank(message = "Password is required")
//        @Size(min = 6, message = "Password must be at least 6 characters")
//        String password,

        @NotBlank(message = "Role is required")
        String role
) {
}
