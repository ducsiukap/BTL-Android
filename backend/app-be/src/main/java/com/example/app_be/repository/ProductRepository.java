package com.example.app_be.repository;

import com.example.app_be.model.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("""
            SELECT p FROM Product p
                WHERE p.name LIKE %:query%
                OR p.description LIKE %:query%
            ORDER BY p.name
            """)
    List<Product> searchProduct(
            @Param("query") String query,
            Pageable pageable
    );

    @Query("""
                SELECT p FROM Product p 
                    WHERE p.catalog.id = :id 
                    AND (p.name LIKE %:query%
                        OR p.description LIKE %:query%)
                    ORDER BY p.name
            """)
    List<Product> searchProduct(
            @Param("id") Long id,
            @Param("query") String query,
            Pageable pageable
    );

}
