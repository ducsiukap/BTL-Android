package com.example.ddht.data.remote.api;

import com.example.ddht.data.remote.dto.ApiResponse;
import com.example.ddht.data.remote.dto.ProductDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ProductApi {
    @GET("products")
    Call<ApiResponse<List<ProductDto>>> searchProducts(
            @Query("query") String query,
            @Query("catalogId") Long catalogId,
            @Query("page") Integer page,
            @Query("size") Integer size
    );
}
