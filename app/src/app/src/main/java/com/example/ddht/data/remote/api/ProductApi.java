package com.example.ddht.data.remote.api;

import com.example.ddht.data.remote.dto.ApiResponse;
import com.example.ddht.data.remote.dto.ModifySaleOffRequest;
import com.example.ddht.data.remote.dto.ProductDto;
import com.example.ddht.data.remote.dto.SaleOffDto;
import com.example.ddht.data.remote.dto.UpdateProductRequest;

import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Query;
import okhttp3.RequestBody;

public interface ProductApi {
    @GET("products")
    Call<ApiResponse<List<ProductDto>>> searchProducts(
            @Query("query") String query,
            @Query("catalogId") Long catalogId,
            @Query("page") Integer page,
            @Query("size") Integer size
    );

        @GET("products/{id}")
        Call<ApiResponse<ProductDto>> getProductById(
            @Path("id") Long productId
        );

        @PUT("products/{id}")
        Call<ApiResponse<ProductDto>> updateProduct(
            @Path("id") Long productId,
                @Body UpdateProductRequest request,
            @Header("Authorization") String bearerToken
        );

        @DELETE("products/{id}")
        Call<ApiResponse<Void>> deleteProduct(
            @Path("id") Long productId,
            @Header("Authorization") String bearerToken
        );

        @POST("products/{id}/saleoffs")
            Call<ApiResponse<SaleOffDto>> addSaleOffToProduct(
            @Path("id") Long productId,
            @Body ModifySaleOffRequest request,
            @Header("Authorization") String bearerToken
        );

            @Multipart
            @POST("products/{id}/images")
            Call<ApiResponse<ProductDto>> addImagesToProduct(
                @Path("id") Long productId,
                @Part List<MultipartBody.Part> images,
                @Header("Authorization") String bearerToken
            );

            @DELETE("products/{productId}/images/{imageId}")
            Call<ApiResponse<ProductDto>> deleteImageFromProduct(
                @Path("productId") Long productId,
                @Path("imageId") Long imageId,
                @Header("Authorization") String bearerToken
            );

        @Multipart
        @POST("catalogs/{id}/products")
        Call<ApiResponse<ProductDto>> addProductToCatalog(
            @Path("id") Long catalogId,
            @Part("name") RequestBody name,
            @Part("description") RequestBody description,
            @Part("price") RequestBody price,
            @Part("isSelling") RequestBody isSelling,
                @Part List<MultipartBody.Part> images,
            @Header("Authorization") String bearerToken
        );

        @DELETE("products/{productId}/saleoffs/{saleOffId}")
        Call<ApiResponse<Void>> deleteSaleOffFromProduct(
            @Path("productId") Long productId,
            @Path("saleOffId") Long saleOffId,
            @Header("Authorization") String bearerToken
        );
}
