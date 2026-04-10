package com.example.ddht.data.remote.dto;

import java.util.List;

public class ProductDto {
    private Long id;
    private String name;
    private String description;
    private Double originalPrice;
    private Boolean isSaleOff;
    private Double discountedPrice;
    private List<ProductImageDto> images;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Double getOriginalPrice() {
        return originalPrice;
    }

    public Boolean getSaleOff() {
        return isSaleOff;
    }

    public Double getDiscountedPrice() {
        return discountedPrice;
    }

    public List<ProductImageDto> getImages() {
        return images;
    }
}
