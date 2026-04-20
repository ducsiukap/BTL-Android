package com.example.app_be.component;

import com.example.app_be.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class OrderScheduler {

    private final OrderService orderService;

    @Scheduled(fixedDelay = 60000)
    public void cancelStaleOrders() {
        try {
            orderService.cancelStaleOrders();
        } catch (Exception e) {
            log.error("Lỗi khi tự động hủy đơn hàng cũ: {}", e.getMessage());
        }
    }
}
