package com.example.app_be.model;

public enum OrderStatus {
    PENDING,    // Chờ xử lý
    PREPARING,  // Đang chế biến
    READY,      // Sẵn sàng nhận đồ
    COMPLETED,  // Hoàn thành
    CANCELLED   // Hủy
}
