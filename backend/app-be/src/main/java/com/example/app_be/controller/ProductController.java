package com.example.app_be.controller;

import com.example.app_be.controller.dto.request.AddImageRequest;
import com.example.app_be.controller.dto.request.CreateSaleOffRequest;
import com.example.app_be.controller.dto.request.SearchProductRequest;
import com.example.app_be.controller.dto.request.UpdateProductRequest;
import com.example.app_be.controller.dto.response.ApiResponse;
import com.example.app_be.controller.dto.response.ProductResponse;
import com.example.app_be.controller.dto.response.SaleOffResponse;
import com.example.app_be.core.annotation.ApiV1;
import com.example.app_be.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@ApiV1
@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductResponse>>> searchProducts(SearchProductRequest request) {
        List<ProductResponse> products = productService.searchProduct(request);
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(
            @PathVariable Long id
    ) {
        ProductResponse product = productService.getProductById(id);
        return ResponseEntity.ok(ApiResponse.success(product));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable("id") Long productId,
            @Valid @RequestBody UpdateProductRequest request
    ) {
        ProductResponse product = productService.updateProduct(productId, request);
        return ResponseEntity.ok(ApiResponse.success(product));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @PathVariable("id") Long productId
    ) {
        productService.deleteProduct(productId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/{id}/images")
    public ResponseEntity<ApiResponse<ProductResponse>> addImageToProduct(
            @PathVariable("id") Long productId,
            @Valid @ModelAttribute AddImageRequest request
    ) {
        ProductResponse product = productService.addProductImage(productId, request);
        return ResponseEntity.ok(ApiResponse.success(product));
    }

    @DeleteMapping("/{productId}/images/{imageId}")
    public ResponseEntity<ApiResponse<ProductResponse>> deleteImageFromProduct(
            @PathVariable Long productId,
            @PathVariable Long imageId
    ) {
        ProductResponse product = productService.deleteProductImage(productId, imageId);
        return ResponseEntity.ok(ApiResponse.success(product));
    }

    @PostMapping("/{id}/saleoffs")
    public ResponseEntity<ApiResponse<SaleOffResponse>> addSaleOffToProduct(
            @PathVariable("id") Long productId,
            @Valid @RequestBody CreateSaleOffRequest request
    ) {
        SaleOffResponse product = productService.addSaleOff(productId, request);
        return ResponseEntity.ok(ApiResponse.success(product));
    }

    @DeleteMapping("/{productId}/saleoffs/{saleOffId}")
    public ResponseEntity<ApiResponse<Void>> deleteSaleOffFromProduct(
            @PathVariable Long productId,
            @PathVariable Long saleOffId
    ) {
        productService.deleteSaleOff(productId, saleOffId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
