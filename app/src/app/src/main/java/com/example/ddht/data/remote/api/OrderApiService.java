package com.example.ddht.data.remote.api;

import com.example.ddht.data.remote.dto.ApiResponse;
import com.example.ddht.data.remote.dto.CreateOrderRequest;
import com.example.ddht.data.remote.dto.OrderResponse;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface OrderApiService {
    @POST("orders")
    Call<ApiResponse<OrderResponse>> createOrder(@Body CreateOrderRequest request);

    @GET("orders/code/{code}")
    Call<ApiResponse<OrderResponse>> getOrderByCode(@Path("code") String code);

    @GET("orders/staff-queue")
    Call<ApiResponse<List<OrderResponse>>> getStaffQueueOrders(@Header("Authorization") String bearerToken);

    @PUT("orders/{id}/status")
    Call<ApiResponse<OrderResponse>> updateOrderStatus(
            @Path("id") Long id, 
            @Query("status") String status,
            @Header("Authorization") String bearerToken
    );

    @PUT("orders/{id}/paid")
    Call<ApiResponse<OrderResponse>> markAsPaid(
            @Path("id") Long id,
            @Header("Authorization") String bearerToken
    );
}
