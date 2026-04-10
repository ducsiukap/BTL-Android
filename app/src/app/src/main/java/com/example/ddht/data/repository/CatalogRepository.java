package com.example.ddht.data.repository;

import com.example.ddht.data.remote.NetworkClient;
import com.example.ddht.data.remote.api.CatalogApi;
import com.example.ddht.data.remote.dto.ApiResponse;
import com.example.ddht.data.remote.dto.CatalogDto;
import com.example.ddht.data.remote.dto.ModifyCatalogRequest;

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

    public Call<ApiResponse<CatalogDto>> createCatalog(String name, String accessToken) {
        return catalogApi.createCatalog(new ModifyCatalogRequest(name), "Bearer " + accessToken);
    }

    public Call<ApiResponse<CatalogDto>> updateCatalog(Long catalogId, String name, String accessToken) {
        return catalogApi.updateCatalog(catalogId, new ModifyCatalogRequest(name), "Bearer " + accessToken);
    }

    public Call<ApiResponse<Void>> deleteCatalog(Long catalogId, String accessToken) {
        return catalogApi.deleteCatalog(catalogId, "Bearer " + accessToken);
    }
}
