package com.example.ddht.data.remote.dto;

import java.math.BigDecimal;

public class StaffStatisticResponse {
    private String staffId;
    private String staffName;
    private Long assignedOrders;
    private Long paidOrders;
    private BigDecimal revenue;

    public String getStaffId() {
        return staffId;
    }

    public String getStaffName() {
        return staffName;
    }

    public Long getAssignedOrders() {
        return assignedOrders;
    }

    public Long getPaidOrders() {
        return paidOrders;
    }

    public BigDecimal getRevenue() {
        return revenue;
    }
}