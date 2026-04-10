package com.example.ddht.data.repository;

import com.example.ddht.data.remote.NetworkClient;
import com.example.ddht.data.remote.api.ProductApi;
import com.example.ddht.data.remote.dto.ApiResponse;
import com.example.ddht.data.remote.dto.ProductDto;

import java.util.List;

import retrofit2.Call;

public class ProductRepository {
    private final ProductApi productApi;

    public ProductRepository() {
        productApi = NetworkClient.getRetrofit().create(ProductApi.class);
    }

    public Call<ApiResponse<List<ProductDto>>> searchProducts(String query, Long catalogId, Integer page, Integer size) {
        return productApi.searchProducts(query, catalogId, page, size);
    }

    public Call<ApiResponse<Void>> deleteSaleOffFromProduct(Long productId, Long saleOffId, String accessToken) {
        return productApi.deleteSaleOffFromProduct(productId, saleOffId, "Bearer " + accessToken);
    }
}
