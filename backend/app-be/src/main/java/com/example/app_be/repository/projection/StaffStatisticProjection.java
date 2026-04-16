package com.example.app_be.repository.projection;

import java.math.BigDecimal;

public interface StaffStatisticProjection {
    String getStaffId();
    String getStaffName();
    Long getAssignedOrders();
    Long getPaidOrders();
    BigDecimal getRevenue();
}
