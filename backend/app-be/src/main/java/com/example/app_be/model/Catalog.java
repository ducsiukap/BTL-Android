package com.example.app_be.model;

import com.example.app_be.model.base.LongIdBaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "catalog")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Catalog extends LongIdBaseEntity {
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @OneToMany(
            mappedBy = "catalog",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            orphanRemoval = false
    )
    @Builder.Default
    private List<Product> products = new ArrayList<>();

    public void addProduct(Product product) {
        this.products.add(product);
        product.setCatalog(this);
    }

    public void removeProduct(Product product) {
        this.products.remove(product);
        product.setCatalog(null);
    }
}
