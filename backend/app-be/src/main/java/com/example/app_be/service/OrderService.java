package com.example.app_be.service;

import com.example.app_be.controller.dto.request.CreateOrderRequest;
import com.example.app_be.controller.dto.response.OrderResponse;
import com.example.app_be.model.OrderStatus;

import java.util.List;
import java.util.UUID;

public interface OrderService {
    OrderResponse createOrder(CreateOrderRequest request);
    
    OrderResponse getOrderByCode(String code);
    
    OrderResponse updateStatus(Long id, OrderStatus status, UUID staffId);

    OrderResponse markAsPaid(Long id);

    List<OrderResponse> getStaffQueueOrders(List<OrderStatus> statuses);

    OrderResponse cancelOrder(Long id, UUID staffId);

    void cancelStaleOrders();
}
