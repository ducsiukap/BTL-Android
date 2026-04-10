package com.example.app_be.service.impl.v1;

import com.example.app_be.controller.dto.request.*;
import com.example.app_be.controller.dto.response.ProductImageResponse;
import com.example.app_be.controller.dto.response.ProductResponse;
import com.example.app_be.controller.dto.response.SaleOffResponse;
import com.example.app_be.core.exception.ResourceNotFoundException;
import com.example.app_be.model.Product;
import com.example.app_be.model.ProductImage;
import com.example.app_be.model.SaleOff;
import com.example.app_be.repository.ProductImageRepository;
import com.example.app_be.repository.ProductRepository;
import com.example.app_be.repository.SaleOffRepository;
import com.example.app_be.service.CloudinaryService;
import com.example.app_be.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final SaleOffRepository saleOffRepository;
    private final CloudinaryService cloudinaryService;
    private final ProductImageRepository productImageRepository;

    @Override
    public List<ProductResponse> searchProduct(SearchProductRequest request) {
        String query = request.query() == null ? "" : request.query();
        Pageable pageable = PageRequest.of(request.page(), request.size());

        List<Product> products = request.catalogId() == null
                ? productRepository.searchProduct(query, pageable)
                : productRepository.searchProduct(request.catalogId(), query, pageable);
        List<Long> productIds = products.stream().map(Product::getId).toList();
        List<SaleOff> saleOffs = saleOffRepository.findAllActiveByProductIds(productIds);

        Map<Long, List<SaleOff>> saleOffMap = saleOffs.stream()
                .collect(Collectors.groupingBy(s -> s.getProduct().getId()));

        return products.stream().map(product ->
                        toProductResponse(product, saleOffMap.getOrDefault(product.getId(), List.of())))
                .toList();
    }


    @Override
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Product not found!")
        );
        return toProductResponse(product);
    }

    private ProductResponse toProductResponse(Product product) {
        List<SaleOff> saleOffs = saleOffRepository.findAllProductActiveSaleOff(product.getId());
        return toProductResponse(product, saleOffs);
    }

    private ProductResponse toProductResponse(Product product, List<SaleOff> saleOffs) {
        BigDecimal totalDiscount = saleOffs.stream()
                .reduce(BigDecimal.ZERO,
                        (acc, saleOff) -> acc.add(saleOff.getDiscount()),
                        BigDecimal::add);
        BigDecimal totalPrice = totalDiscount.compareTo(product.getPrice()) > 0
                ? BigDecimal.ZERO
                : product.getPrice().subtract(totalDiscount);

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .isSelling(product.isSelling())
                .originalPrice(product.getPrice())
                .isSaleOff(totalDiscount.compareTo(BigDecimal.ZERO) > 0)
                .discountedPrice(totalPrice)
                .images(
                        product.getImages().stream().map(productImage ->
                                ProductImageResponse.builder()
                                        .id(productImage.getId())
                                        .url(productImage.getUrl())
                                        .build()
                        ).toList()
                ).build();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ProductResponse updateProduct(Long id, UpdateProductRequest request) {
        Product product = productRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Product not found!")
        );

        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setSelling(request.isSelling());

        return toProductResponse(product);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Product not found!")
        );
        productRepository.delete(product);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ProductResponse addProductImage(Long productId, AddImageRequest request) {
        Product product = productRepository.findById(productId).orElseThrow(
                () -> new ResourceNotFoundException("Product not found!")
        );

        if (product.getImages().size() + request.images().size() > 3)
            throw new IllegalArgumentException("Maximum number of images is 3!");

        request.images().forEach(image -> {
            Map result = cloudinaryService.upload(image);

            String url = (String) result.get("secure_url");
            String publicId = (String) result.get("public_id");
            product.addImage(
                    ProductImage.builder().url(url).publicId(publicId).build()
            );
        });
        productRepository.saveAndFlush(product);
        return toProductResponse(product);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ProductResponse deleteProductImage(Long productId, Long imageId) {
        Product product = productRepository.findById(productId).orElseThrow(
                () -> new ResourceNotFoundException("Product not found!")
        );

        ProductImage image = productImageRepository.findById(imageId).orElseThrow(
                () -> new ResourceNotFoundException("Product image not found!")
        );
        if (!Objects.equals(product.getId(), productId))
            throw new IllegalArgumentException("Product image not found!");

        product.removeImage(image);

        return toProductResponse(product);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public SaleOffResponse addSaleOff(Long productId, CreateSaleOffRequest request) {
        Product product = productRepository.findById(productId).orElseThrow(
                () -> new ResourceNotFoundException("Product not found!")
        );

        if (product.getPrice().compareTo(request.discount()) < 0)
            throw new IllegalArgumentException("Discount cannot be greater than product price!");


        SaleOff saleOff = SaleOff.builder().startDate(request.startDate())
                .endDate(request.endDate())
                .discount(request.discount())
                .isActive(request.isActive()).build();
        product.addSaleOff(saleOff);
        saleOffRepository.saveAndFlush(saleOff);

        return new SaleOffResponse(
                saleOff.getId(), saleOff.getDiscount(),
                saleOff.getStartDate(), saleOff.getEndDate(),
                saleOff.isActive(), product.getId(), product.getName()
        );
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteSaleOff(Long productId, Long saleOffId) {
        Product product = productRepository.findById(productId).orElseThrow(
                () -> new ResourceNotFoundException("Product not found!")
        );

        SaleOff saleOff = saleOffRepository.findById(saleOffId).orElseThrow(
                () -> new ResourceNotFoundException("Sale off not found!")
        );
        product.removeSaleOff(saleOff);
    }
}
