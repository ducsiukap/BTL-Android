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
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.*;
import java.time.temporal.ChronoUnit;
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
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderResponse createOrder(CreateOrderRequest request) {
        // 1. Sinh mã đơn hàng
        String code = generateOrderCode();

        // 2. Tạo đối tượng Order
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

        OrderResponse response = toOrderResponse(savedOrder);

        // [WS] Thông báo cho nhân viên có đơn hàng mới
        messagingTemplate.convertAndSend("/topic/staff/new-order", response);

        return response;
    }

    @Override
    public OrderResponse getOrderByCode(String code) {
        Order order = orderRepository.findByCodeContainingIgnoreCase(code.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with code: " + code));
        return toOrderResponse(order);
    }

    @Override
    public List<OrderResponse> getStaffQueueOrders(List<OrderStatus> statuses) {
        if (statuses != null && !statuses.isEmpty()) {
            return orderRepository.findByStatusInOrderByCreatedAtDesc(statuses)
                    .stream()
                    .map(order -> toOrderResponse(order))
                    .collect(Collectors.toList());
        }
        return orderRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(order -> toOrderResponse(order))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderResponse updateStatus(Long id, OrderStatus status, UUID staffId) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        validateStatusTransition(order.getStatus(), status);

        // Lưu nhân viên thực hiện
        if (order.getUser() == null) {
            User staff = userRepository.findById(staffId)
                    .orElseThrow(() -> new ResourceNotFoundException("Staff not found with id: " + staffId));
            order.setUser(staff);
        }

        order.setStatus(status);
        Order savedOrder = orderRepository.save(order);
        OrderResponse response = toOrderResponse(savedOrder);

        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderResponse markAsPaid(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
        
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Đơn hàng đang ở trạng thái không thể thanh toán.");
        }

        order.setPaid(true);
        order.setPaymentTime(Instant.now());
        order.setStatus(OrderStatus.PREPARING);
        
        Order savedOrder = orderRepository.save(order);
        OrderResponse response = toOrderResponse(savedOrder);
        messagingTemplate.convertAndSend("/topic/order/" + savedOrder.getCode(), response);
        
        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderResponse cancelOrder(Long id, UUID staffId) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        // Nhân viên hủy đơn
        if (order.getUser() == null) {
            User staff = userRepository.findById(staffId)
                    .orElseThrow(() -> new ResourceNotFoundException("Staff not found with id: " + staffId));
            order.setUser(staff);
        }

        order.setStatus(OrderStatus.CANCELLED);
        Order savedOrder = orderRepository.save(order);
        
        // Thông báo cho khách qua WS nếu đơn bị hủy
        messagingTemplate.convertAndSend("/topic/order/" + savedOrder.getCode(), toOrderResponse(savedOrder));
        
        return toOrderResponse(savedOrder);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelStaleOrders() {
        Instant tenMinsAgo = Instant.now().minus(10, ChronoUnit.MINUTES);
        List<Order> staleOrders = orderRepository.findByStatusAndCreatedAtBefore(OrderStatus.PENDING, tenMinsAgo);
        
        for (Order order : staleOrders) {
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);
            messagingTemplate.convertAndSend("/topic/order/" + order.getCode(), toOrderResponse(order));
        }
    }


    private void validateStatusTransition(OrderStatus current, OrderStatus target) {
        if (current == target) return;

        if (current == OrderStatus.CANCELLED || current == OrderStatus.COMPLETED) {
            throw new IllegalStateException("Đơn hàng đã kết thúc, không thể đổi trạng thái.");
        }

        switch (target) {
            case PREPARING:
                break;
            case COMPLETED:
                if (current != OrderStatus.PREPARING) {
                    throw new IllegalStateException("Đơn hàng không thể chuyển sang HOÀN THÀNH.");
                }
                break;
            case CANCELLED:
                if (current != OrderStatus.PENDING && current != OrderStatus.PREPARING) {
                    throw new IllegalStateException("Không thể hủy đơn hàng ở trạng thái này.");
                }
                break;
        }
    }

    // Logic sinh mã đơn hàng: OD-xxx
    private String generateOrderCode() {
        long count = orderRepository.count();
        return String.format("OD-%03d", count + 1);
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
