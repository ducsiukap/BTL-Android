package com.example.ddht.data.remote.dto;

public class UpdateProductRequest {
    private final String name;
    private final String description;
    private final Double price;
    private final Boolean isSelling;

    public UpdateProductRequest(String name, String description, Double price, Boolean isSelling) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.isSelling = isSelling;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Double getPrice() {
        return price;
    }

    public Boolean getSelling() {
        return isSelling;
    }
}
