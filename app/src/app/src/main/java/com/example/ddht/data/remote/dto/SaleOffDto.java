package com.example.ddht.data.remote.dto;

public class SaleOffDto {
    private Long id;
    private Double discount;
    private String startDate;
    private String endDate;
    private Boolean isActive;
    private Long productId;
    private String productName;

    public Long getId() {
        return id;
    }

    public Double getDiscount() {
        return discount;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public Boolean getActive() {
        return isActive;
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }
}
