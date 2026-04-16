package com.example.app_be.controller.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductStatisticResponse {
    private Long productId;
    private String productName;
    private Long catalogId;
    private String catalogName;
    private Long soldQuantity;
    private BigDecimal revenue;
}