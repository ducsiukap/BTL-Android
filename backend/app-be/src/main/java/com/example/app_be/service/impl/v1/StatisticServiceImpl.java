package com.example.app_be.service.impl.v1;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.app_be.controller.dto.response.CatalogStatisticResponse;
import com.example.app_be.controller.dto.response.ProductStatisticResponse;
import com.example.app_be.controller.dto.response.StaffStatisticResponse;
import com.example.app_be.controller.dto.response.StatisticOverViewResponse;
import com.example.app_be.controller.dto.response.StatusStatisticResponse;
import com.example.app_be.controller.dto.response.TimeSeriesPointResponse;
import com.example.app_be.model.OrderStatus;
import com.example.app_be.repository.StatisticRepository;
import com.example.app_be.repository.projection.CatalogStatisticProjection;
import com.example.app_be.repository.projection.ProductStatisticProjection;
import com.example.app_be.repository.projection.StaffStatisticProjection;
import com.example.app_be.repository.projection.StatusCountProjection;
import com.example.app_be.repository.projection.TimeSeriesProjection;
import com.example.app_be.service.StatisticService;
import com.example.app_be.service.statistic.ProductStatisticSortBy;
import com.example.app_be.service.statistic.StatisticGroupBy;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatisticServiceImpl implements StatisticService {

     private StatisticRepository statisticRepository;

     @Override
     public StatisticOverViewResponse getOverView(Instant from, Instant to) {

          validateRange(from, to);

          BigDecimal totalRevenue = statisticRepository.sumRevenue(from, to);
          Long totalOrders = statisticRepository.countAllOrders(from, to);
          Long paidOrders = statisticRepository.countPaidOrders(from, to);
          Long completedOrders = statisticRepository.countCompletedOrders(from, to);
          Long canceledOrders = statisticRepository.countCanceledOrders(from, to);

          BigDecimal averageOrderValue = paidOrders == 0
                    ? BigDecimal.ZERO
                    : totalRevenue.divide(BigDecimal.valueOf(paidOrders), 2, RoundingMode.HALF_UP);

          BigDecimal cancelRate = percent(canceledOrders, totalOrders);

          BigDecimal completionRate = percent(completedOrders, totalOrders);

          return new StatisticOverViewResponse(totalRevenue, totalOrders, paidOrders, completedOrders, canceledOrders,
                    averageOrderValue, cancelRate, completionRate);
     }

     @Override
     public List<TimeSeriesPointResponse> getRevenueSeries(Instant from, Instant to, StatisticGroupBy groupBy) {
          validateRange(from, to);

          List<TimeSeriesProjection> rows = switch (groupBy) {
               case DAY -> statisticRepository.revenueSeriesByDay(from, to);
               case WEEK -> statisticRepository.revenueSeriesByWeek(from, to);
               case MONTH -> statisticRepository.revenueSeriesByMonth(from, to);
               case QUARTER -> statisticRepository.revenueSeriesByQuarter(from, to);
               case YEAR -> statisticRepository.revenueSeriesByYear(from, to);
          };

          return rows.stream().map(r -> new TimeSeriesPointResponse(r.getBucket(), nonNull(r.getRevenue()),
                    nonNull(r.getOrderCount()))).toList();
     }

     @Override
     public List<TimeSeriesPointResponse> getOrderSeries(Instant from, Instant to, StatisticGroupBy groupBy) {
          validateRange(from, to);

          List<TimeSeriesProjection> rows = switch (groupBy) {
               case DAY -> statisticRepository.orderSeriesByDay(from, to);
               case WEEK -> statisticRepository.orderSeriesByMonth(from, to);
               case MONTH -> statisticRepository.orderSeriesByMonth(from, to);
               case QUARTER -> statisticRepository.orderSeriesByQuarter(from, to);
               case YEAR -> statisticRepository.orderSeriesByYear(from, to);
          };

          return rows.stream().map(
                    r -> new TimeSeriesPointResponse(r.getBucket(), nonNull(r.getRevenue()),
                              nonNull(r.getOrderCount())))
                    .toList();
     }

     @Override
     public List<ProductStatisticResponse> getTopProducts(Instant from, Instant to, int limit,
               ProductStatisticSortBy sortBy) {
          validateRange(from, to);

          if (limit <= 0) {
               throw new IllegalArgumentException("limit must be greater than 0");
          }

          List<ProductStatisticProjection> rows = sortBy == ProductStatisticSortBy.QUANTITY
                    ? statisticRepository.topProductsByQuantity(from, to, limit)
                    : statisticRepository.topProductsByRevenue(from, to, limit);
          return rows.stream().map(
                    r -> ProductStatisticResponse.builder()
                              .productId(r.getProductId())
                              .productName(r.getProductName())
                              .catalogId(r.getCatalogId())
                              .catalogName(r.getCatalogName())
                              .soldQuantity(nonNull(r.getSoldQuantity()))
                              .revenue(nonNull(r.getRevenue()))
                              .build())
                    .toList();
     }

     @Override
     public List<CatalogStatisticResponse> getCatalogStatistics(Instant from, Instant to) {
          validateRange(from, to);

          List<CatalogStatisticProjection> rows = statisticRepository.catalogStatistics(from, to);

          return rows.stream().map(
                    r -> CatalogStatisticResponse.builder()
                              .catalogId(r.getCatalogId())
                              .catalogName(r.getCatalogName())
                              .revenue(nonNull(r.getRevenue()))
                              .soldQuantity(nonNull(r.getSoldQuantity()))
                              .build())
                    .toList();
     }

     @Override
     public List<StaffStatisticResponse> getStaffStatistics(Instant from, Instant to) {
          validateRange(from, to);

          List<StaffStatisticProjection> rows = statisticRepository.staffStatistics(from, to);

          return rows.stream().map(
                    r -> new StaffStatisticResponse(toUuid(r.getStaffId()), r.getStaffName(),
                              nonNull(r.getAssignedOrders()), nonNull(r.getPaidOrders()), nonNull(r.getRevenue())))
                    .toList();
     }

     @Override
     public List<StatusStatisticResponse> getStatusDistribution(Instant from, Instant to) {
          validateRange(from, to);

          List<StatusCountProjection> rows = statisticRepository.statusDistribution(from, to);

          Long total = nonNull(statisticRepository.countAllOrders(from, to));

          return rows.stream().map(
                    r -> new StatusStatisticResponse(OrderStatus.valueOf(r.getStatus()), nonNull(r.getCount()),
                              percent(r.getCount(), total)))
                    .toList();
     }

     private void validateRange(Instant from, Instant to) {
          if (from == null || to == null) {
               throw new IllegalArgumentException("from/to is required");
          }

          if (!from.isBefore(to)) {
               throw new IllegalArgumentException("from must be before to");
          }
     }

     private BigDecimal percent(Long part, Long total) {
          if (total <= 0) {
               return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
          }

          return BigDecimal.valueOf(part).multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
     }

     private BigDecimal nonNull(BigDecimal value) {
          return value == null ? BigDecimal.ZERO : value;
     }

     private long nonNull(Long value) {
          return value == null ? 0L : value;
     }

     private UUID toUuid(String raw) {
          return raw == null ? null : UUID.fromString(raw);
     }

}