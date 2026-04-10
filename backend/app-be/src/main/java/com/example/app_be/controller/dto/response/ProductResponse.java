package com.example.app_be.controller.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Builder
public record ProductResponse(
        long id, String name, String description, boolean isSelling,
        BigDecimal originalPrice, boolean isSaleOff, BigDecimal discountedPrice,
        List<ProductImageResponse> images
) {
}
