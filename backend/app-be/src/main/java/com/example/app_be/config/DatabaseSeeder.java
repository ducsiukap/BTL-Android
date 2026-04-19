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

        // 2. Seed Catalogs (10 food-centric categories)
        Catalog rice = Catalog.builder().name("Cơm").build();
        Catalog noodles = Catalog.builder().name("Phở & Bún").build();
        Catalog bread = Catalog.builder().name("Bánh mì").build();
        Catalog appetizers = Catalog.builder().name("Món khai vị").build();
        Catalog mainDishes = Catalog.builder().name("Món chính").build();
        Catalog snacks = Catalog.builder().name("Đồ ăn vặt").build();
        Catalog salads = Catalog.builder().name("Salad & Rau").build();
        Catalog desserts = Catalog.builder().name("Tráng miệng").build();
        Catalog drinks = Catalog.builder().name("Đồ uống").build();
        Catalog combos = Catalog.builder().name("Combo tiết kiệm").build();

        catalogRepository.saveAll(List.of(rice, noodles, bread, appetizers, mainDishes, snacks, salads, desserts, drinks, combos));

        // 3. Seed Products (At least 3 per catalog)
        List<Product> allProducts = new ArrayList<>();

        // Rice
        Product p1 = Product.builder().name("Cơm tấm sườn bì chả").description("Cơm tấm đặc sản Sài Gòn").price(new BigDecimal("45000")).catalog(rice).isSelling(true).build();
        Product p31 = Product.builder().name("Cơm gà Hải Nam").description("Gà luộc mềm, cơm thơm mỡ gà").price(new BigDecimal("55000")).catalog(rice).isSelling(true).build();
        Product p32 = Product.builder().name("Cơm rang dưa bò").description("Cơm rang giòn kèm dưa chua và thịt bò").price(new BigDecimal("50000")).catalog(rice).isSelling(true).build();
        allProducts.addAll(List.of(p1, p31, p32));

        // Noodles
        Product p2 = Product.builder().name("Phở bò tái lăn").description("Phở bò truyền thống Hà Nội").price(new BigDecimal("45000")).catalog(noodles).isSelling(true).build();
        Product p33 = Product.builder().name("Bún chả Hà Nội").description("Thịt nướng than hoa, bún tươi, nước mắm chua ngọt").price(new BigDecimal("40000")).catalog(noodles).isSelling(true).build();
        Product p34 = Product.builder().name("Bún bò Huế").description("Đặc sản cố đô với vị sả ớt đậm đà").price(new BigDecimal("50000")).catalog(noodles).isSelling(true).build();
        allProducts.addAll(List.of(p2, p33, p34));

        // Bread
        Product p3 = Product.builder().name("Bánh mì heo quay").description("Bánh mì giòn nhân thịt heo quay da giòn").price(new BigDecimal("30000")).catalog(bread).isSelling(true).build();
        Product p35 = Product.builder().name("Bánh mì xá xíu").description("Bánh mì kẹp thịt xá xíu đậm đà").price(new BigDecimal("25000")).catalog(bread).isSelling(true).build();
        Product p36 = Product.builder().name("Bánh mì chảo").description("Bánh mì kèm pate, trứng ốp la, xúc xích và sốt đặc biệt").price(new BigDecimal("45000")).catalog(bread).isSelling(true).build();
        allProducts.addAll(List.of(p3, p35, p36));

        // Appetizers
        Product p5 = Product.builder().name("Nem rán").description("Nem rán truyền thống miền Bắc").price(new BigDecimal("35000")).catalog(appetizers).isSelling(true).build();
        Product p37 = Product.builder().name("Phở cuốn").description("Phở cuốn thịt bò rau thơm").price(new BigDecimal("40000")).catalog(appetizers).isSelling(true).build();
        Product p38 = Product.builder().name("Gỏi cuốn tôm thịt").description("Món ăn tươi mát kèm nước chấm tương đen").price(new BigDecimal("30000")).catalog(appetizers).isSelling(true).build();
        allProducts.addAll(List.of(p5, p37, p38));

        // Main Dishes
        Product p39 = Product.builder().name("Gà kho sả ớt").description("Thịt gà ta kho sả ớt cay nồng").price(new BigDecimal("65000")).catalog(mainDishes).isSelling(true).build();
        Product p40 = Product.builder().name("Sườn xào chua ngọt").description("Sườn non rim sốt chua ngọt đậm đà").price(new BigDecimal("70000")).catalog(mainDishes).isSelling(true).build();
        Product p41 = Product.builder().name("Cá kho tộ").description("Cá lóc kho tộ kiểu miền Tây").price(new BigDecimal("75000")).catalog(mainDishes).isSelling(true).build();
        allProducts.addAll(List.of(p39, p40, p41));

        // Snacks
        Product p42 = Product.builder().name("Khoai tây chiên").description("Khoai tây chiên giòn rụm").price(new BigDecimal("25000")).catalog(snacks).isSelling(true).build();
        Product p43 = Product.builder().name("Mực khô nướng").description("Mực khô nướng xé sợi").price(new BigDecimal("80000")).catalog(snacks).isSelling(true).build();
        Product p44 = Product.builder().name("Chân gà sả tắc").description("Chân gà giòn sần sật ngâm sả tắc").price(new BigDecimal("45000")).catalog(snacks).isSelling(true).build();
        allProducts.addAll(List.of(p42, p43, p44));

        // Salads
        Product p45 = Product.builder().name("Salad dầu giấm").description("Rau xà lách trộn sốt dầu giấm").price(new BigDecimal("30000")).catalog(salads).isSelling(true).build();
        Product p46 = Product.builder().name("Rau muống xào tỏi").description("Rau muống giòn, thơm mùi tỏi phi").price(new BigDecimal("25000")).catalog(salads).isSelling(true).build();
        Product p47 = Product.builder().name("Nộm đu đủ bò khô").description("Gỏi đu đủ xanh giòn kèm bò khô").price(new BigDecimal("35000")).catalog(salads).isSelling(true).build();
        allProducts.addAll(List.of(p45, p46, p47));

        // Desserts
        Product p48 = Product.builder().name("Chè bưởi").description("Chè bưởi An Giang cốt dừa béo ngậy").price(new BigDecimal("20000")).catalog(desserts).isSelling(true).build();
        Product p49 = Product.builder().name("Bánh flan").description("Bánh flan mềm mịn, thơm mùi trứng sữa").price(new BigDecimal("15000")).catalog(desserts).isSelling(true).build();
        Product p50 = Product.builder().name("Hoa quả dầm").description("Trái cây tươi theo mùa dầm sữa chua").price(new BigDecimal("30000")).catalog(desserts).isSelling(true).build();
        allProducts.addAll(List.of(p48, p49, p50));

        // Drinks
        Product p51 = Product.builder().name("Cà phê sữa đá").description("Cà phê pha phin truyền thống Việt Nam").price(new BigDecimal("25000")).catalog(drinks).isSelling(true).build();
        Product p52 = Product.builder().name("Trà chanh sả").description("Trà tươi giải nhiệt thơm mùi sả").price(new BigDecimal("20000")).catalog(drinks).isSelling(true).build();
        Product p53 = Product.builder().name("Nước cam ép").description("Cam tươi vắt nguyên chất").price(new BigDecimal("35000")).catalog(drinks).isSelling(true).build();
        allProducts.addAll(List.of(p51, p52, p53));

        // Combo
        Product p54 = Product.builder().name("Combo Cơm trưa").description("1 Cơm tấm + 1 Trà chanh").price(new BigDecimal("60000")).catalog(combos).isSelling(true).build();
        Product p55 = Product.builder().name("Combo Ăn sáng").description("1 Phở bò + 1 Cà phê đá").price(new BigDecimal("65000")).catalog(combos).isSelling(true).build();
        Product p56 = Product.builder().name("Set lẩu mini").description("Set lẩu 1 người đủ vị").price(new BigDecimal("99000")).catalog(combos).isSelling(true).build();
        allProducts.addAll(List.of(p54, p55, p56));

        productRepository.saveAll(allProducts);

        // 4. Seed SaleOffs (15 products with SaleOff, 5 products have 3+ SaleOff records)
        List<SaleOff> allSaleOffs = new ArrayList<>();

        // 5 Products with 3+ SaleOff records
        Product[] multiSaleProducts = {p1, p2, p3, p39, p54};
        for (Product product : multiSaleProducts) {
            allSaleOffs.add(SaleOff.builder().product(product).discount(new BigDecimal("2000")).startDate(Instant.now().minus(30, ChronoUnit.DAYS)).endDate(Instant.now().minus(20, ChronoUnit.DAYS)).isActive(false).build());
            allSaleOffs.add(SaleOff.builder().product(product).discount(new BigDecimal("5000")).startDate(Instant.now().minus(1, ChronoUnit.DAYS)).endDate(Instant.now().plus(7, ChronoUnit.DAYS)).isActive(true).build());
            allSaleOffs.add(SaleOff.builder().product(product).discount(new BigDecimal("3000")).startDate(Instant.now().plus(10, ChronoUnit.DAYS)).endDate(Instant.now().plus(20, ChronoUnit.DAYS)).isActive(true).build());
        }

        // 10 more products with at least 1 SaleOff record
        Product[] singleSaleProducts = {p5, p31, p33, p36, p40, p43, p47, p48, p51, p56};
        for (Product product : singleSaleProducts) {
            allSaleOffs.add(SaleOff.builder().product(product).discount(new BigDecimal("4000")).startDate(Instant.now().minus(2, ChronoUnit.DAYS)).endDate(Instant.now().plus(10, ChronoUnit.DAYS)).isActive(true).build());
        }

        saleOffRepository.saveAll(allSaleOffs);

        // 5. Seed Orders (Remapping existing logic to new food items)
        Order order1 = Order.builder()
                .code("OD-20260414-001")
                .status(OrderStatus.PENDING)
                .isPaid(false)
                .totalPrice(new BigDecimal("90000")) // Updated total for new prices (p1: 45k, p2: 45k)
                .items(new ArrayList<>())
                .build();

        OrderItem item1 = OrderItem.builder()
                .order(order1)
                .product(p1) // Cơm tấm sườn
                .quantity(1)
                .price(new BigDecimal("45000"))
                .build();

        OrderItem item2 = OrderItem.builder()
                .order(order1)
                .product(p2) // Phở bò
                .quantity(1)
                .price(new BigDecimal("45000"))
                .build();

        order1.addItem(item1);
        order1.addItem(item2);

        Order order2 = Order.builder()
                .code("OD-20260414-002")
                .status(OrderStatus.COMPLETED)
                .isPaid(true)
                .paymentTime(Instant.now())
                .user(staff)
                .totalPrice(new BigDecimal("25000")) // p3: 30k - 5k = 25k (Bánh mì heo quay)
                .items(new ArrayList<>())
                .build();

        OrderItem item3 = OrderItem.builder()
                .order(order2)
                .product(p3)
                .quantity(1)
                .price(new BigDecimal("25000"))
                .build();

        order2.addItem(item3);

        orderRepository.saveAll(List.of(order1, order2));

        log.info("Database seeding completed successfully.");
    }
}
