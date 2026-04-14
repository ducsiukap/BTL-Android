package com.example.app_be.repository;

import com.example.app_be.model.Order;
import com.example.app_be.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByCode(String code);

    List<Order> findByUserId(UUID userId);

    List<Order> findByStatus(OrderStatus status);

    // Đếm số lượng đơn hàng được tạo trong khoảng thời gian
    // Dùng để reset bộ đếm xxx theo tháng
    long countByCreatedAtBetween(Instant start, Instant end);
}
