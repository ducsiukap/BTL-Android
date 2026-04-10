package com.example.app_be.controller.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;

public record CreateSaleOffRequest(
        @NotNull(message = "Product is required")
        @DecimalMin(value = "0", message = "Discount must be greater than 0")
        BigDecimal discount,

        @NotNull(message = "Start date is required")
        Instant startDate,

        Instant endDate,

        Boolean isActive
) {

    public CreateSaleOffRequest {
        if (isActive == null) isActive = false;
    }
}
