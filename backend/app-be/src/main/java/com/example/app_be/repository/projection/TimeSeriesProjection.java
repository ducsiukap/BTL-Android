package com.example.app_be.repository.projection;

import java.math.BigDecimal;

public interface TimeSeriesProjection {
    String getBucket();
    BigDecimal getRevenue();
    Long getOrderCount();
}
