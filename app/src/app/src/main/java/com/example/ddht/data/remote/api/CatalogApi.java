package com.example.ddht.data.remote.api;

import com.example.ddht.data.remote.dto.ApiResponse;
import com.example.ddht.data.remote.dto.CatalogDto;
import com.example.ddht.data.remote.dto.ModifyCatalogRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import retrofit2.http.POST;
import retrofit2.http.PUT;

public interface CatalogApi {
    @GET("catalogs")
    Call<ApiResponse<List<CatalogDto>>> getAllCatalogs();

    @POST("catalogs")
    Call<ApiResponse<CatalogDto>> createCatalog(@Body ModifyCatalogRequest request, @Header("Authorization") String bearerToken);

    @PUT("catalogs/{id}")
    Call<ApiResponse<CatalogDto>> updateCatalog(@Path("id") Long catalogId,
                                                @Body ModifyCatalogRequest request,
                                                @Header("Authorization") String bearerToken);

    @DELETE("catalogs/{id}")
    Call<ApiResponse<Void>> deleteCatalog(@Path("id") Long catalogId, @Header("Authorization") String bearerToken);
}
