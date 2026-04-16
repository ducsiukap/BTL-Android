package com.example.ddht.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class OrderItemRequest {
    @SerializedName("productId")
    private final Long productId;
    
    @SerializedName("quantity")
    private final int quantity;

    public OrderItemRequest(Long productId, int quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    public Long getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }
}
