package com.example.ddht.data.repository;

import com.example.ddht.data.remote.NetworkClient;
import com.example.ddht.data.remote.api.OrderApiService;
import com.example.ddht.data.remote.dto.ApiResponse;
import com.example.ddht.data.remote.dto.CreateOrderRequest;
import com.example.ddht.data.remote.dto.OrderResponse;

import java.util.List;
import retrofit2.Call;

public class OrderRepository {
    private final OrderApiService orderApiService;

    public OrderRepository() {
        orderApiService = NetworkClient.getRetrofit().create(OrderApiService.class);
    }

    public Call<ApiResponse<OrderResponse>> createOrder(CreateOrderRequest request) {
        return orderApiService.createOrder(request);
    }

    public Call<ApiResponse<OrderResponse>> getOrderByCode(String code) {
        return orderApiService.getOrderByCode(code);
    }

    public Call<ApiResponse<List<OrderResponse>>> getStaffQueueOrders(String token) {
        return orderApiService.getStaffQueueOrders(token);
    }

    public Call<ApiResponse<OrderResponse>> updateOrderStatus(Long id, String status, String token) {
        return orderApiService.updateOrderStatus(id, status, token);
    }

    public Call<ApiResponse<OrderResponse>> markAsPaid(Long id, String token) {
        return orderApiService.markAsPaid(id, token);
    }
}
