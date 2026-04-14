package com.example.app_be.service.impl.v1;

import com.example.app_be.controller.dto.request.CreateOrderRequest;
import com.example.app_be.controller.dto.request.OrderItemRequest;
import com.example.app_be.controller.dto.response.OrderItemResponse;
import com.example.app_be.controller.dto.response.OrderResponse;
import com.example.app_be.core.exception.ResourceNotFoundException;
import com.example.app_be.model.*;
import com.example.app_be.repository.*;
import com.example.app_be.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final SaleOffRepository saleOffRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderResponse createOrder(CreateOrderRequest request) {
        // 1. Sinh mã đơn hàng
        String code = generateOrderCode();

        // 2. Tạo đối tượng Order cơ bản
        Order order = Order.builder()
                .code(code)
                .status(OrderStatus.PENDING)
                .isPaid(false)
                .totalPrice(BigDecimal.ZERO)
                .items(new ArrayList<>())
                .build();

        BigDecimal totalPrice = BigDecimal.ZERO;

        // 3. Xử lý từng item
        for (OrderItemRequest itemReq : request.items()) {
            Product product = productRepository.findById(itemReq.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + itemReq.productId()));

            // Tính giá snapshot (áp dụng sale off)
            BigDecimal finalPrice = calculateDiscountedPrice(product);
            
            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .quantity(itemReq.quantity())
                    .price(finalPrice)
                    .order(order)
                    .build();

            order.addItem(orderItem);
            totalPrice = totalPrice.add(finalPrice.multiply(BigDecimal.valueOf(itemReq.quantity())));
        }

        order.setTotalPrice(totalPrice);

        // 4. Lưu vào Database
        Order savedOrder = orderRepository.save(order);

        return toOrderResponse(savedOrder);
    }

    @Override
    public OrderResponse getOrderByCode(String code) {
        Order order = orderRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with code: " + code));
        return toOrderResponse(order);
    }

    @Override
    public List<OrderResponse> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status)
                .stream().map(this::toOrderResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderResponse updateStatus(Long id, OrderStatus status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        order.setStatus(status);
        return toOrderResponse(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderResponse markAsPaid(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        
        order.setPaid(true);
        order.setPaymentTime(Instant.now());
        // Khi thanh toán tại quầy, thường chuyển sang chuẩn bị đồ luôn
        if (order.getStatus() == OrderStatus.PENDING) {
            order.setStatus(OrderStatus.PREPARING);
        }
        
        return toOrderResponse(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderResponse assignStaff(Long id, UUID staffId) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        
        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found"));
        
        order.setUser(staff);
        return toOrderResponse(order);
    }

    // Logic sinh mã đơn hàng: OD-YYYYMMDD-xxx
    private String generateOrderCode() {
        LocalDate now = LocalDate.now();
        String datePart = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        
        // Lấy khoảng thời gian của tháng hiện tại để reset bộ đếm xxx
        YearMonth yearMonth = YearMonth.from(now);
        Instant startOfMonth = yearMonth.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endOfMonth = yearMonth.atEndOfMonth().atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();
        
        long count = orderRepository.countByCreatedAtBetween(startOfMonth, endOfMonth);
        String sequencePart = String.format("%03d", count + 1);
        
        return "OD-" + datePart + "-" + sequencePart;
    }

    private BigDecimal calculateDiscountedPrice(Product product) {
        List<SaleOff> saleOffs = saleOffRepository.findAllProductActiveSaleOff(product.getId());
        BigDecimal totalDiscount = saleOffs.stream()
                .reduce(BigDecimal.ZERO, (acc, s) -> acc.add(s.getDiscount()), BigDecimal::add);
        
        BigDecimal discountedPrice = product.getPrice().subtract(totalDiscount);
        return discountedPrice.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : discountedPrice;
    }

    private OrderResponse toOrderResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(item -> new OrderItemResponse(
                        item.getId(),
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getQuantity(),
                        item.getPrice(),
                        item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
                )).collect(Collectors.toList());

        return new OrderResponse(
                order.getId(),
                order.getCode(),
                order.getTotalPrice(),
                order.getStatus(),
                order.isPaid(),
                order.getPaymentTime(),
                order.getCreatedAt(),
                order.getUser() != null ? order.getUser().getId() : null,
                order.getUser() != null ? order.getUser().getFullName() : null,
                itemResponses
        );
    }
}
