package com.example.ddht.data.remote.dto;

import java.math.BigDecimal;

public class ProductStatisticResponse {
     private Long productId;
     private String productName;
     private Long catalogId;
     private String catalogName;
     private Long soldQuantity;
     private BigDecimal revenue;

    public Long getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public Long getCatalogId() {
        return catalogId;
    }

    public String getCatalogName() {
        return catalogName;
    }

    public Long getSoldQuantity() {
        return soldQuantity;
    }

    public BigDecimal getRevenue() {
        return revenue;
    }
}
