package com.example.app_be.config;

import com.example.app_be.model.*;
import com.example.app_be.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CatalogRepository catalogRepository;
    private final ProductRepository productRepository;
    private final SaleOffRepository saleOffRepository;
    private final OrderRepository orderRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Database already seeded. Skipping...");
            return;
        }

        log.info("Starting database seeding...");

        // 1. Seed Users
        User manager = User.builder()
                .email("manager@example.com")
                .password(passwordEncoder.encode("12345678"))
                .fullName("Quản lý Admin")
                .role(UserRole.MANAGER)
                .build();
        
        User staff = User.builder()
                .email("staff@example.com")
                .password(passwordEncoder.encode("12345678"))
                .fullName("Nhân viên Phục vụ 1")
                .role(UserRole.STAFF)
                .build();

        userRepository.saveAll(List.of(manager, staff));

        // 2. Seed Catalogs
        Catalog coffee = Catalog.builder().name("Cà phê").build();
        Catalog tea = Catalog.builder().name("Trà sữa").build();
        Catalog food = Catalog.builder().name("Đồ ăn").build();

        catalogRepository.saveAll(List.of(coffee, tea, food));

        // 3. Seed Products
        Product p1 = Product.builder().name("Bạc xỉu").description("Cà phê sữa nhiều sữa").price(new BigDecimal("30000")).catalog(coffee).isSelling(true).build();
        Product p2 = Product.builder().name("Cà phê đen").description("Cà phê nguyên chất đậm đà").price(new BigDecimal("20000")).catalog(coffee).isSelling(true).build();
        Product p3 = Product.builder().name("Trà sữa Trân châu").description("Trà sữa kèm trân châu đen dai giòn").price(new BigDecimal("45000")).catalog(tea).isSelling(true).build();
        Product p4 = Product.builder().name("Trà sữa Matcha").description("Vị trà xanh Nhật Bản").price(new BigDecimal("50000")).catalog(tea).isSelling(true).build();
        Product p5 = Product.builder().name("Bánh mì Pate").description("Bánh mì giòn nhân pate gan").price(new BigDecimal("25000")).catalog(food).isSelling(true).build();

        productRepository.saveAll(List.of(p1, p2, p3, p4, p5));

        // 4. Seed SaleOffs
        SaleOff sale = SaleOff.builder()
                .product(p3)
                .discount(new BigDecimal("5000"))
                .startDate(Instant.now().minus(1, ChronoUnit.DAYS))
                .endDate(Instant.now().plus(7, ChronoUnit.DAYS))
                .isActive(true)
                .build();
        saleOffRepository.save(sale);

        // 5. Seed Orders
        // Order 1: Pending
        Order order1 = Order.builder()
                .code("OD-20260414-001")
                .status(OrderStatus.PENDING)
                .isPaid(false)
                .totalPrice(new BigDecimal("50000"))
                .items(new ArrayList<>())
                .build();
        
        OrderItem item1 = OrderItem.builder()
                .order(order1)
                .product(p1) // Bạc xỉu 30k
                .quantity(1)
                .price(new BigDecimal("30000"))
                .build();
        
        OrderItem item2 = OrderItem.builder()
                .order(order1)
                .product(p2) // Cafe đen 20k
                .quantity(1)
                .price(new BigDecimal("20000"))
                .build();
        
        order1.addItem(item1);
        order1.addItem(item2);

        // Order 2: Completed & Paid
        Order order2 = Order.builder()
                .code("OD-20260414-002")
                .status(OrderStatus.COMPLETED)
                .isPaid(true)
                .paymentTime(Instant.now())
                .user(staff) // Gán cho staff xử lý
                .totalPrice(new BigDecimal("40000")) // Giả sử Trà sữa trân châu đang giảm 5k còn 40k
                .items(new ArrayList<>())
                .build();

        OrderItem item3 = OrderItem.builder()
                .order(order2)
                .product(p3)
                .quantity(1)
                .price(new BigDecimal("40000"))
                .build();

        order2.addItem(item3);

        orderRepository.saveAll(List.of(order1, order2));

        log.info("Database seeding completed successfully.");
    }
}
