package com.example.ddht.data.remote.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ProductDto {
    @SerializedName("id")
    private Long id;

    @SerializedName("name")
    private String name;

    @SerializedName("description")
    private String description;

    @SerializedName(value = "selling", alternate = {"isSelling"})
    private Boolean isSelling;

    @SerializedName("originalPrice")
    private Double originalPrice;

    @SerializedName(value = "saleOff", alternate = {"isSaleOff"})
    private Boolean isSaleOff;

    @SerializedName("discountedPrice")
    private Double discountedPrice;

    @SerializedName("images")
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

    public void setSelling(Boolean selling) {
        isSelling = selling;
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
