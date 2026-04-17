package com.example.app_be.controller;

import java.time.Instant;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.app_be.controller.dto.response.ApiResponse;
import com.example.app_be.controller.dto.response.CatalogStatisticResponse;
import com.example.app_be.controller.dto.response.ProductStatisticResponse;
import com.example.app_be.controller.dto.response.StaffStatisticResponse;
import com.example.app_be.controller.dto.response.StatisticOverViewResponse;
import com.example.app_be.controller.dto.response.StatusStatisticResponse;
import com.example.app_be.controller.dto.response.TimeSeriesPointResponse;
import com.example.app_be.core.annotation.ApiV1;
import com.example.app_be.service.impl.v1.StatisticServiceImpl;
import com.example.app_be.service.statistic.ProductStatisticSortBy;
import com.example.app_be.service.statistic.StatisticGroupBy;

import lombok.RequiredArgsConstructor;

@ApiV1
@RestController
@RequestMapping("/statistics")
@RequiredArgsConstructor
public class StatisticController {

     private final StatisticServiceImpl statisticService;

     @GetMapping("/overview")
     public ResponseEntity<ApiResponse<StatisticOverViewResponse>> getOverview(
               @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
               @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
          System.out.println("statistic overview");
          return ResponseEntity.ok().body(ApiResponse.success(statisticService.getOverView(from, to)));
     }

     @GetMapping("/revenue-series")
     public ResponseEntity<ApiResponse<List<TimeSeriesPointResponse>>> getRevenueSeries(
               @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
               @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
               @RequestParam(name = "groupBy", defaultValue = "MONTH") StatisticGroupBy groupBy) {
          System.out.println("revenue-series");
          return ResponseEntity.ok().body(ApiResponse.success(statisticService.getRevenueSeries(from, to, groupBy)));
     }

     @GetMapping("/order-series")
     public ResponseEntity<ApiResponse<List<TimeSeriesPointResponse>>> getOrderSeries(
               @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
               @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
               @RequestParam(defaultValue = "MONTH") StatisticGroupBy groupBy) {
          System.out.println("order-series");
          return ResponseEntity.ok().body(ApiResponse.success(statisticService.getOrderSeries(from, to, groupBy)));
     }

     @GetMapping("/top-products")
     public ResponseEntity<ApiResponse<List<ProductStatisticResponse>>> getTopProducts(
               @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
               @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
               @RequestParam(name = "limit", defaultValue = "10") int limit,
               @RequestParam(name = "sortBy", defaultValue = "REVENUE") ProductStatisticSortBy sortBy) {
          System.out.println("top-products");
          return ResponseEntity.ok()
                    .body(ApiResponse.success(statisticService.getTopProducts(from, to, limit, sortBy)));
     }

     @GetMapping("/by-catalog")
     public ResponseEntity<ApiResponse<List<CatalogStatisticResponse>>> getByCatalog(
               @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
               @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
          System.out.println("by-catalog");
          return ResponseEntity.ok().body(ApiResponse.success(statisticService.getCatalogStatistics(from, to)));
     }

     @GetMapping("/by-staff")
     public ResponseEntity<ApiResponse<List<StaffStatisticResponse>>> getByStaff(
               @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
               @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
          System.out.println("by-staff");
          return ResponseEntity.ok().body(ApiResponse.success(statisticService.getStaffStatistics(from, to)));
     }

     @GetMapping("/status-distribution")
     public ResponseEntity<ApiResponse<List<StatusStatisticResponse>>> getstatusDistribution(
               @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
               @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
          System.out.println("status-distribution");
          return ResponseEntity.ok().body(ApiResponse.success(statisticService.getStatusDistribution(from, to)));
     }
}
