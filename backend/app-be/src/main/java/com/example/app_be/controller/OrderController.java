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
            @Valid @RequestBody CreateOrderRequest request) {
        return ResponseEntity.ok(ApiResponse.success(orderService.createOrder(request)));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderByCode(
            @PathVariable("code") String code) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getOrderByCode(code)));
    }

    @GetMapping("/staff-queue")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getStaffQueueOrders(
            @RequestParam(value = "status", required = false) List<OrderStatus> status) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getStaffQueueOrders(status)));
    }

    @PutMapping("/{id}/paid")
    public ResponseEntity<ApiResponse<OrderResponse>> markAsPaid(
            @PathVariable("id") Long id) {
        return ResponseEntity.ok(ApiResponse.success(orderService.markAsPaid(id)));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateStatus(
            @PathVariable("id") Long id,
            @RequestParam("status") OrderStatus status,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ApiResponse.success(orderService.updateStatus(id, status, currentUser.getId())));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ApiResponse.success(orderService.cancelOrder(id, currentUser.getId())));
    }

}
