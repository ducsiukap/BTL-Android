package com.example.ddht.data.repository;

import com.example.ddht.data.remote.NetworkClient;
import com.example.ddht.data.remote.api.CatalogApi;
import com.example.ddht.data.remote.dto.ApiResponse;
import com.example.ddht.data.remote.dto.CatalogDto;

import java.util.List;

import retrofit2.Call;

public class CatalogRepository {
    private final CatalogApi catalogApi;

    public CatalogRepository() {
        catalogApi = NetworkClient.getRetrofit().create(CatalogApi.class);
    }

    public Call<ApiResponse<List<CatalogDto>>> getAllCatalogs() {
        return catalogApi.getAllCatalogs();
    }
}
