package com.example.ddht.data.remote.dto;

import java.math.BigDecimal;

public class StatusStatisticResponse {
    private OrderStatus status;
    private Long count;
    private BigDecimal ratioPercent;

    public OrderStatus getStatus() {
        return status;
    }

    public Long getCount() {
        return count;
    }

    public BigDecimal getRatioPercent() {
        return ratioPercent;
    }
}