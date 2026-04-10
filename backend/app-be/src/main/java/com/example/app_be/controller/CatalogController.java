package com.example.app_be.controller;

import com.example.app_be.controller.dto.request.CreateProductRequest;
import com.example.app_be.controller.dto.request.ModifyCatalogRequest;
import com.example.app_be.controller.dto.response.ApiResponse;
import com.example.app_be.controller.dto.response.CatalogResponse;
import com.example.app_be.controller.dto.response.ProductResponse;
import com.example.app_be.core.annotation.ApiV1;
import com.example.app_be.service.CatalogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@ApiV1
@RestController
@RequestMapping("/catalogs")
@RequiredArgsConstructor
public class CatalogController {

    private final CatalogService catalogService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CatalogResponse>>> getAllCatalogs() {
        List<CatalogResponse> catalogs = catalogService.getAllCatalogs();
        return ResponseEntity.ok(ApiResponse.success(catalogs));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CatalogResponse>> createCatalog(
            @Valid @RequestBody ModifyCatalogRequest request
    ) {
        CatalogResponse catalog = catalogService.createCatalog(request);
        return ResponseEntity.ok(ApiResponse.success(catalog));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CatalogResponse>> updateCatalog(
            @PathVariable Long id,
            @Valid @RequestBody ModifyCatalogRequest request
    ) {
        CatalogResponse catalog = catalogService.updateCatalog(id, request);
        return ResponseEntity.ok(ApiResponse.success(catalog));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCatalog(
            @PathVariable Long id
    ) {
        catalogService.deleteCatalog(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping(value = "/{id}/products", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ProductResponse>> addProductToCatalog(
            @PathVariable Long id,
            @Valid @ModelAttribute CreateProductRequest request
    ) {
        ProductResponse product = catalogService.createProduct(id, request);
        return ResponseEntity.ok(ApiResponse.success(product));
    }

}
