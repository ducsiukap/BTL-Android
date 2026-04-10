package com.example.ddht.data.remote.api;

import com.example.ddht.data.remote.dto.ApiResponse;
import com.example.ddht.data.remote.dto.CatalogDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface CatalogApi {
    @GET("catalogs")
    Call<ApiResponse<List<CatalogDto>>> getAllCatalogs();
}
