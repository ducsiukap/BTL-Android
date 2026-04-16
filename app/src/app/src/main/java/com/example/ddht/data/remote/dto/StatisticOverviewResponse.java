package com.example.ddht.data.remote.dto;

import java.math.BigDecimal;

public class StatisticOverviewResponse {
     private BigDecimal totalRevenue;

     private Long totalOrders;

     private Long paidOrders;

     private Long completedOrders;

     private Long cancelledOrders;

     private BigDecimal averageOrderValue;

     private BigDecimal cancelRatePercent;

     private BigDecimal completionRatePercent;

     public BigDecimal getTotalRevenue() {
          return totalRevenue;
     }

     public Long getTotalOrders() {
          return totalOrders;
     }

     public Long getPaidOrders() {
          return paidOrders;
     }

     public Long getCompletedOrders() {
          return completedOrders;
     }

     public Long getCancelledOrders() {
          return cancelledOrders;
     }

     public BigDecimal getAverageOrderValue() {
          return averageOrderValue;
     }

     public BigDecimal getCancelRatePercent() {
          return cancelRatePercent;
     }

     public BigDecimal getCompletionRatePercent() {
          return completionRatePercent;
     }

}
