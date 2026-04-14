package com.example.app_be.service;

import com.example.app_be.controller.dto.request.CreateOrderRequest;
import com.example.app_be.controller.dto.response.OrderResponse;
import com.example.app_be.model.OrderStatus;

import java.util.List;
import java.util.UUID;

public interface OrderService {
    OrderResponse createOrder(CreateOrderRequest request);
    
    OrderResponse getOrderByCode(String code);
    
    List<OrderResponse> getOrdersByStatus(OrderStatus status);
    
    OrderResponse updateStatus(Long id, OrderStatus status);
    
    OrderResponse markAsPaid(Long id);

    OrderResponse assignStaff(Long id, UUID staffId);
}
