package com.example.app_be.controller.dto.response;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long id,
        Long productId,
        String productName,
        Integer quantity,
        BigDecimal price,
        BigDecimal subtotal
) {
}
