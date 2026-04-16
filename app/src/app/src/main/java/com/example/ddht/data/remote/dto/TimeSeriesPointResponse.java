package com.example.ddht.data.remote.dto;

import java.math.BigDecimal;

public class TimeSeriesPointResponse {
     private String bucket;

     private BigDecimal revenue;

     private Long orderAmount;

     public String getBucket() {
          return bucket;
     }

     public BigDecimal getRevenue() {
          return revenue;
     }

     public Long getOrderAmount() {
          return orderAmount;
     }

}
