package com.example.app_be.service.impl.v1;

import com.example.app_be.controller.dto.request.CreateProductRequest;
import com.example.app_be.controller.dto.request.ModifyCatalogRequest;
import com.example.app_be.controller.dto.response.CatalogResponse;
import com.example.app_be.controller.dto.response.ProductImageResponse;
import com.example.app_be.controller.dto.response.ProductResponse;
import com.example.app_be.core.exception.ResourceNotFoundException;
import com.example.app_be.model.Catalog;
import com.example.app_be.model.Product;
import com.example.app_be.model.ProductImage;
import com.example.app_be.repository.CatalogRepository;
import com.example.app_be.repository.ProductRepository;
import com.example.app_be.service.CatalogService;
import com.example.app_be.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CatalogServiceImpl implements CatalogService {

    private final CatalogRepository catalogRepository;
    private final ProductRepository productRepository;
    private final CloudinaryService cloudinaryService;

    @Override
    public List<CatalogResponse> getAllCatalogs() {
        List<Catalog> catalogs = catalogRepository.findAll();
        return catalogs.stream()
                .map(catalog ->
                        new CatalogResponse(catalog.getId(), catalog.getName()))
                .toList();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public CatalogResponse createCatalog(ModifyCatalogRequest request) {
        Catalog catalog = Catalog.builder()
                .name(request.name())
                .build();
        catalog = catalogRepository.save(catalog);

        return new CatalogResponse(catalog.getId(), catalog.getName());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ProductResponse createProduct(Long id, CreateProductRequest request) {
        if (request.getImages().size() > 3)
            throw new IllegalArgumentException("Maximum number of images is 3!");

        Catalog catalog = catalogRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Catalog not found!")
        );


        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .isSelling(request.getIsSelling())
                .build();

        catalog.addProduct(product);
//        catalogRepository.saveAndFlush(catalog);

        request.getImages().forEach(image -> {
            Map result = cloudinaryService.upload(image);

            String url = (String) result.get("secure_url");
            String publicId = (String) result.get("public_id");

            product.addImage(
                    ProductImage.builder()
                            .url(url)
                            .publicId(publicId)
                            .build()
            );
        });
        productRepository.saveAndFlush(product);

        return ProductResponse.builder()
                .id(product.getId()).name(product.getName())
                .description(product.getDescription())
                .isSelling(product.isSelling())
                .originalPrice(product.getPrice())
                .isSaleOff(false)
                .discountedPrice(product.getPrice())
                .images(
                        product.getImages().stream().map(productImage ->
                                        new ProductImageResponse(productImage.getId(), productImage.getUrl()))
                                .toList()
                )
                .build();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public CatalogResponse updateCatalog(Long id, ModifyCatalogRequest request) {
        Catalog catalog = catalogRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Catalog not found!")
        );

        catalog.setName(request.name());
        catalogRepository.save(catalog);

        return CatalogResponse.builder()
                .id(catalog.getId())
                .name(catalog.getName())
                .build();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteCatalog(Long id) {
        Catalog catalog = catalogRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Catalog not found!")
        );

        catalogRepository.delete(catalog);
    }
}
