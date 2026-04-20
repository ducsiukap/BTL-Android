package com.example.app_be.controller.dto.response;

import com.example.app_be.model.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        Long id,
        String code,
        BigDecimal totalPrice,
        OrderStatus status,
        boolean isPaid,
        Instant paymentTime,
        Instant createdAt,
        UUID staffId,
        String staffName,
        List<OrderItemResponse> items
) {
}
