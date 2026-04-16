package com.example.ddht.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class OrderItemResponse {
    @SerializedName("id")
    private Long id;
    
    @SerializedName("productId")
    private Long productId;
    
    @SerializedName("productName")
    private String productName;
    
    @SerializedName("quantity")
    private int quantity;
    
    @SerializedName("unitPrice")
    private double unitPrice;
    
    @SerializedName("totalPrice")
    private double totalPrice;

    public Long getId() { return id; }
    public Long getProductId() { return productId; }
    public String getProductName() { return productName; }
    public int getQuantity() { return quantity; }
    public double getUnitPrice() { return unitPrice; }
    public double getTotalPrice() { return totalPrice; }
}
