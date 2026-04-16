package com.example.app_be.controller.dto.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CatalogStatisticResponse {
    private Long catalogId;
    private String catalogName;
    private Long soldQuantity;
    private BigDecimal revenue;
}
