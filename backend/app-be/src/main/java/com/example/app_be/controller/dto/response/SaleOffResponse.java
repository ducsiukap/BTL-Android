package com.example.app_be.controller.dto.response;

import java.math.BigDecimal;
import java.time.Instant;


public record SaleOffResponse (
        long id, BigDecimal discount,
        Instant startDate, Instant endDate,
        boolean isActive, Long productId, String productName
){
}
