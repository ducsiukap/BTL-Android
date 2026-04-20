package com.example.ddht.data.remote.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class OrderResponse {
    @SerializedName("id")
    private Long id;
    
    @SerializedName("code")
    private String code;
    
    @SerializedName("totalPrice")
    private double totalPrice;
    
    @SerializedName("status")
    private OrderStatus status;
    
    @SerializedName("isPaid")
    private boolean isPaid;
    
    @SerializedName("items")
    private List<OrderItemResponse> items;

    public Long getId() { return id; }
    public String getCode() { return code; }
    public double getTotalPrice() { return totalPrice; }
    public OrderStatus getStatus() { return status; }
    public boolean isPaid() { return isPaid; }
    public List<OrderItemResponse> getItems() { return items; }
}
