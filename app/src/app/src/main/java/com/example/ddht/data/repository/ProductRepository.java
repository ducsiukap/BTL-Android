package com.example.ddht.data.repository;

import com.example.ddht.data.remote.NetworkClient;
import com.example.ddht.data.remote.api.ProductApi;
import com.example.ddht.data.remote.dto.ApiResponse;
import com.example.ddht.data.remote.dto.ModifySaleOffRequest;
import com.example.ddht.data.remote.dto.ProductDto;
import com.example.ddht.data.remote.dto.SaleOffDto;
import com.example.ddht.data.remote.dto.UpdateProductRequest;

import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;

public class ProductRepository {
    private final ProductApi productApi;

    public ProductRepository() {
        productApi = NetworkClient.getRetrofit().create(ProductApi.class);
    }

    public Call<ApiResponse<List<ProductDto>>> searchProducts(String query, Long catalogId, Integer page, Integer size) {
        return productApi.searchProducts(query, catalogId, page, size);
    }

    public Call<ApiResponse<ProductDto>> getProductById(Long productId) {
        return productApi.getProductById(productId);
    }

    public Call<ApiResponse<ProductDto>> updateProduct(Long productId,
                                                       String name,
                                                       String description,
                                                       Double price,
                                                       Boolean isSelling,
                                                       String accessToken) {
        UpdateProductRequest request = new UpdateProductRequest(name, description, price, isSelling);
        return productApi.updateProduct(productId, request, "Bearer " + accessToken);
    }

    public Call<ApiResponse<Void>> deleteProduct(Long productId, String accessToken) {
        return productApi.deleteProduct(productId, "Bearer " + accessToken);
    }

    public Call<ApiResponse<SaleOffDto>> addSaleOffToProduct(Long productId,
                                                             Double discount,
                                                             String startDate,
                                                             String endDate,
                                                             Boolean isActive,
                                                             String accessToken) {
        ModifySaleOffRequest request = new ModifySaleOffRequest(discount, startDate, endDate, isActive);
        return productApi.addSaleOffToProduct(productId, request, "Bearer " + accessToken);
    }

    public Call<ApiResponse<ProductDto>> addProductToCatalog(Long catalogId,
                                                             String name,
                                                             String description,
                                                             Double price,
                                                             Boolean isSelling,
                                                             List<MultipartBody.Part> images,
                                                             String accessToken) {
        return productApi.addProductToCatalog(
                catalogId,
                toTextPart(name),
                toTextPart(description == null ? "" : description),
                toTextPart(String.valueOf(price)),
                toTextPart(String.valueOf(isSelling)),
                images,
                "Bearer " + accessToken
        );
    }

    public Call<ApiResponse<ProductDto>> addImagesToProduct(Long productId,
                                                            List<MultipartBody.Part> images,
                                                            String accessToken) {
        return productApi.addImagesToProduct(productId, images, "Bearer " + accessToken);
    }

    public Call<ApiResponse<ProductDto>> deleteImageFromProduct(Long productId,
                                                                Long imageId,
                                                                String accessToken) {
        return productApi.deleteImageFromProduct(productId, imageId, "Bearer " + accessToken);
    }

    public Call<ApiResponse<Void>> deleteSaleOffFromProduct(Long productId, Long saleOffId, String accessToken) {
        return productApi.deleteSaleOffFromProduct(productId, saleOffId, "Bearer " + accessToken);
    }

    private RequestBody toTextPart(String value) {
        return RequestBody.create(MediaType.parse("text/plain"), value == null ? "" : value);
    }
}
