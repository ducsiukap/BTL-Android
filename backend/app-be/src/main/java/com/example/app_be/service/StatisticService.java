package com.example.app_be.service;

import java.time.Instant;
import java.util.List;

import com.example.app_be.controller.dto.response.CatalogStatisticResponse;
import com.example.app_be.controller.dto.response.ProductStatisticResponse;
import com.example.app_be.controller.dto.response.StaffStatisticResponse;
import com.example.app_be.controller.dto.response.StatisticOverViewResponse;
import com.example.app_be.controller.dto.response.StatusStatisticResponse;
import com.example.app_be.controller.dto.response.TimeSeriesPointResponse;
import com.example.app_be.service.statistic.ProductStatisticSortBy;
import com.example.app_be.service.statistic.StatisticGroupBy;

public interface StatisticService {
    public StatisticOverViewResponse getOverView(Instant from, Instant to);

    public List<TimeSeriesPointResponse> getRevenueSeries(Instant from, Instant to, StatisticGroupBy groupBy);

    public List<TimeSeriesPointResponse> getOrderSeries(Instant from, Instant to, StatisticGroupBy groupBy);

    public List<ProductStatisticResponse> getTopProducts(Instant from, Instant to, int limit, ProductStatisticSortBy sortBy);

    public List<CatalogStatisticResponse> getCatalogStatistics(Instant from, Instant to);

    public List<StaffStatisticResponse> getStaffStatistics(Instant from, Instant to);

    public List<StatusStatisticResponse> getStatusDistribution(Instant from, Instant to);
}
