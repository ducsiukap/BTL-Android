package com.example.app_be.repository.projection;

import java.math.BigDecimal;

public interface ProductStatisticProjection {
    Long getProductId();
    String getProductName();
    Long getCatalogId();
    String getCatalogName();
    Long getSoldQuantity();
    BigDecimal getRevenue();
}
