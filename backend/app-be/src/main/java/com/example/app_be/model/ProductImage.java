package com.example.app_be.model;

import com.example.app_be.model.base.LongIdBaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "product_images",
        indexes = {
                @Index(name = "idx_product_image_product", columnList = "product_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"url", "product_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class ProductImage extends LongIdBaseEntity {

    @Column(nullable = false)
    private String url;

    @Column(name = "public_id", nullable = false)
    private String publicId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
}
