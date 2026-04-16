package com.example.app_be.controller.dto.response;

import java.math.BigDecimal;
import java.util.UUID;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StaffStatisticResponse {
    private UUID staffId;
    private String staffName;
    private Long assignedOrders;
    private Long paidOrders;
    private BigDecimal revenue;
}
