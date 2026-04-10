package com.example.app_be.repository;

import com.example.app_be.model.SaleOff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SaleOffRepository extends JpaRepository<SaleOff, Long> {

    @Query("""
            SELECT s FROM SaleOff s WHERE
                s.startDate <= CURRENT_TIMESTAMP
                AND (s.endDate IS NULL OR s.endDate > CURRENT_TIMESTAMP)
                ORDER BY s.startDate
            """)
    List<SaleOff> findAllActiveSaleOffs();

    @Query("""
            SELECT s FROM SaleOff s WHERE s.isActive = true
                    AND s.product.id = :id
                    AND s.startDate <= CURRENT_TIMESTAMP
                    AND (s.endDate IS NULL OR s.endDate > CURRENT_TIMESTAMP)
            """)
    List<SaleOff> findAllProductActiveSaleOff(@Param("id") Long id);

    @Query("""
            SELECT s FROM SaleOff s WHERE s.isActive = true
                AND s.product.id IN :ids
                AND s.startDate <= CURRENT_TIMESTAMP
                AND (s.endDate IS NULL OR s.endDate > CURRENT_TIMESTAMP)
            """)
    List<SaleOff> findAllActiveByProductIds(@Param("ids") List<Long> ids);
}
