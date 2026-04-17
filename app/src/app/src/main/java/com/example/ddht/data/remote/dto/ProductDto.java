package com.example.ddht.data.remote.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ProductDto {
    private Long id;
    private String name;
    private String description;

    @SerializedName("selling")
    private Boolean isSelling;

    private Double originalPrice;

    @SerializedName("saleOff")
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

    public Boolean getSelling() {
        return isSelling;
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
