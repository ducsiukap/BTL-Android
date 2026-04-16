package com.example.app_be.controller.dto.response;

import java.math.BigDecimal;

import com.example.app_be.model.OrderStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StatusStatisticResponse {
    private OrderStatus status;
    private Long count;
    private BigDecimal ratioPercent;
}
