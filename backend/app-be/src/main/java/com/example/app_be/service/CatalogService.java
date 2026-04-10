package com.example.app_be.service;

import com.example.app_be.controller.dto.request.CreateProductRequest;
import com.example.app_be.controller.dto.request.ModifyCatalogRequest;
import com.example.app_be.controller.dto.response.CatalogResponse;
import com.example.app_be.controller.dto.response.ProductResponse;

import java.util.List;

public interface CatalogService {
    public List<CatalogResponse> getAllCatalogs();

    public CatalogResponse createCatalog(ModifyCatalogRequest request);

    public CatalogResponse updateCatalog(Long id, ModifyCatalogRequest request);

    public void deleteCatalog(Long id);

    public ProductResponse createProduct(Long id, CreateProductRequest request);
}
