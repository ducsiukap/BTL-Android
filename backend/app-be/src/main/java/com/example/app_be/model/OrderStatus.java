package com.example.app_be.model;

public enum OrderStatus {
    PENDING,    // Chờ xử lý
    PREPARING,  // Đang chế biến
    COMPLETED,  // Hoàn thành
    CANCELLED   // Hủy
}
