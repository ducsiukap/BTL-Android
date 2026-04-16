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
public class StatisticOverViewResponse {
    private BigDecimal totalRevenue;

    private Long totalOrders;

    private Long paidOrders;

    private Long completedOrders;

    private Long cancelledOrders;

    private BigDecimal averageOrderValue;

    private BigDecimal cancelRatePercent;

    private BigDecimal completionRatePercent;
}
