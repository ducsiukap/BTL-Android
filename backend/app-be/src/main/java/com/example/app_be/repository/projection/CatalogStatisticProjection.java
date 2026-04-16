package com.example.app_be.repository.projection;

import java.math.BigDecimal;

public interface CatalogStatisticProjection {
    Long getCatalogId();
    String getCatalogName();
    Long getSoldQuantity();
    BigDecimal getRevenue();
}
