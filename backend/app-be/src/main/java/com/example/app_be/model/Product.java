package com.example.app_be.model;

import com.example.app_be.model.base.LongIdBaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "products",
        indexes = {
                @Index(name = "idx_product_name", columnList = "name"),
                @Index(name = "idx_product_catalog", columnList = "catalog_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Product extends LongIdBaseEntity {

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(name = "is_selling", nullable = false)
    private boolean isSelling;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "catalog_id")
    private Catalog catalog;

    @OneToMany(
            mappedBy = "product",
            cascade = {CascadeType.ALL},
            orphanRemoval = true
    )
    @Builder.Default
    private List<SaleOff> saleOffs = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductImage> images = new ArrayList<>();


    public void addSaleOff(SaleOff saleOff) {
        this.saleOffs.add(saleOff);
        saleOff.setProduct(this);
    }

    public void addImage(ProductImage image) {
        this.images.add(image);
        image.setProduct(this);
    }

    public void removeSaleOff(SaleOff saleOff) {
        this.saleOffs.remove(saleOff);
        saleOff.setProduct(null);
    }

    public void removeImage(ProductImage image) {
        this.images.remove(image);
        image.setProduct(null);
    }
}
