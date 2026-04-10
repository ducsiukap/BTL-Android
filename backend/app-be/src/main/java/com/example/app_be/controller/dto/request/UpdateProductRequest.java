package com.example.app_be.controller.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record UpdateProductRequest(
        @NotBlank(message = "Product's name is required")
        String name,

        String description,
        @NotNull(message = "Product's price is required")
        @DecimalMin(value = "0", message = "Product's price must be greater than 0")
        BigDecimal price,
        Boolean isSelling
) {

    public UpdateProductRequest {
        if (isSelling == null) isSelling = true;
        if (description == null) description = "";
    }
}
