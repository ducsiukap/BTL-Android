package com.example.app_be.repository;

import com.example.app_be.model.Order;
import com.example.app_be.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status);

    List<Order> findByStatusInOrderByCreatedAtDesc(List<OrderStatus> statuses);

    List<Order> findAllByOrderByCreatedAtDesc();

    long countByCreatedAtBetween(Instant start, Instant end);

    List<Order> findByStatusAndCreatedAtBefore(OrderStatus status, Instant time);

    Optional<Order> findByCodeContainingIgnoreCase(String code);
}
