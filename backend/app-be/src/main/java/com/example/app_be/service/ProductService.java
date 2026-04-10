package com.example.app_be.service;

import com.example.app_be.controller.dto.request.*;
import com.example.app_be.controller.dto.response.ProductResponse;
import com.example.app_be.controller.dto.response.SaleOffResponse;

import java.util.List;

public interface ProductService {

    List<ProductResponse> searchProduct(SearchProductRequest request);

    public ProductResponse getProductById(Long id);

    public ProductResponse updateProduct(Long id, UpdateProductRequest request);

    public void deleteProduct(Long id);

    public ProductResponse addProductImage(Long productId, AddImageRequest request);

    public ProductResponse deleteProductImage(Long productId, Long imageId);

    public SaleOffResponse addSaleOff(Long productId, CreateSaleOffRequest request);

    public void deleteSaleOff(Long productId, Long saleOffId);
}
