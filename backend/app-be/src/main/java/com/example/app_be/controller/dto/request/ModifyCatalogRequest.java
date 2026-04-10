package com.example.app_be.controller.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ModifyCatalogRequest(
        @NotBlank(message = "Catalog's name is required")
        String name
) {
}
