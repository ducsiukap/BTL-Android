package com.example.app_be.controller;

import com.example.app_be.core.annotation.ApiV1;
import com.example.app_be.model.Order;
import com.example.app_be.repository.OrderRepository;
import com.example.app_be.service.OrderService;
import com.example.app_be.service.VNPayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

@ApiV1
@RequestMapping("/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final VNPayService vnPayService;
    private final OrderService orderService;
    private final OrderRepository orderRepository;

    @GetMapping("/vnpay-callback")
    public ResponseEntity<String> vnpayCallback(@RequestParam Map<String, String> allParams) {
        log.info("VNPay Result: {}", allParams);
        
        String vnp_ResponseCode = allParams.get("vnp_ResponseCode");
        String orderCode = allParams.get("vnp_TxnRef");

        if ("00".equals(vnp_ResponseCode)) {
            if (vnPayService.verifyChecksum(new HashMap<>(allParams))) {
                Order order = orderRepository.findByCodeContainingIgnoreCase(orderCode).orElse(null);
                if (order != null && !order.isPaid()) {
                    orderService.markAsPaid(order.getId());
                }
                return ResponseEntity.ok("<h1>Thanh toán thành công!</h1><p>Đơn hàng " + orderCode + " đã được xác nhận. Quay lại App để kiểm tra.</p>");
            } else {
                return ResponseEntity.ok("<h1>Lỗi bảo mật!</h1><p>Chữ ký không hợp lệ.</p>");
            }
        } else {
            return ResponseEntity.ok("<h1>Thanh toán thất bại!</h1><p>Giao dịch đã bị hủy hoặc có lỗi xảy ra.</p>");
        }
    }
}
