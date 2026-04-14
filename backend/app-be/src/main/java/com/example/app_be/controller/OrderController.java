package com.example.app_be.controller;

import com.example.app_be.controller.dto.request.CreateOrderRequest;
import com.example.app_be.controller.dto.response.ApiResponse;
import com.example.app_be.controller.dto.response.OrderResponse;
import com.example.app_be.core.annotation.ApiV1;
import com.example.app_be.model.OrderStatus;
import com.example.app_be.model.User;
import com.example.app_be.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@ApiV1
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @Valid @RequestBody CreateOrderRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(orderService.createOrder(request)));
    }

    @GetMapping("/{code}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderByCode(
            @PathVariable String code
    ) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getOrderByCode(code)));
    }

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getPendingOrders() {
        return ResponseEntity.ok(ApiResponse.success(orderService.getOrdersByStatus(OrderStatus.PENDING)));
    }

    @PutMapping("/{id}/paid")
    public ResponseEntity<ApiResponse<OrderResponse>> markAsPaid(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(ApiResponse.success(orderService.markAsPaid(id)));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateStatus(
            @PathVariable Long id,
            @RequestParam OrderStatus status
    ) {
        return ResponseEntity.ok(ApiResponse.success(orderService.updateStatus(id, status)));
    }

    @PutMapping("/{id}/assign")
    public ResponseEntity<ApiResponse<OrderResponse>> assignMe(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(orderService.assignStaff(id, currentUser.getId())));
    }
}
