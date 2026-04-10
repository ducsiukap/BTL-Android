package com.example.app_be.model;

import com.example.app_be.model.base.LongIdBaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(
        name = "sale_offs",
        indexes = {
                @Index(name = "idx_sale_off_product_time_active",
                        columnList = "product_id, is_active, start_date, end_date"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class SaleOff extends LongIdBaseEntity {
    @Column(name = "start_date", nullable = false)
    private Instant startDate;

    @Column(name = "end_date")
    private Instant endDate;

    @Column(name = "discount", nullable = false)
    private BigDecimal discount;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
}
