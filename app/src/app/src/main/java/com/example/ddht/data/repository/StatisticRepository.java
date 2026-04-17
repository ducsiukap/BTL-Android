package com.example.ddht.data.repository;

import com.example.ddht.data.remote.NetworkClient;
import com.example.ddht.data.remote.api.StatisticApi;
import com.example.ddht.data.remote.dto.ApiResponse;
import com.example.ddht.data.remote.dto.CatalogStatisticResponse;
import com.example.ddht.data.remote.dto.ProductStatisticResponse;
import com.example.ddht.data.remote.dto.StaffStatisticResponse;
import com.example.ddht.data.remote.dto.StatisticOverviewResponse;
import com.example.ddht.data.remote.dto.StatusStatisticResponse;
import com.example.ddht.data.remote.dto.TimeSeriesPointResponse;

import java.time.Instant;
import java.util.List;

import retrofit2.Call;

public class StatisticRepository {

    private final StatisticApi statisticApi;

    public StatisticRepository() {
        statisticApi = NetworkClient.getRetrofit().create(StatisticApi.class);
    }

    private String formatToken(String token) {
        if (token == null) return null;
        if (token.startsWith("Bearer ")) return token;
        return "Bearer " + token;
    }

    public Call<ApiResponse<StatisticOverviewResponse>> getStatisticOverview(String token, Instant from,
            Instant to) {
        return statisticApi.getStatisticOverview(formatToken(token), from, to);
    }

    public Call<ApiResponse<List<TimeSeriesPointResponse>>> getRevenueSeries(String token, Instant from,
            Instant to, String groupBy) {
        return statisticApi.getRevenueSeries(formatToken(token), from, to, groupBy);
    }

    public Call<ApiResponse<List<TimeSeriesPointResponse>>> getOrderSeries(String token, Instant from, Instant to,
            String groupBy) {
        return statisticApi.getOrderSeries(formatToken(token), from, to, groupBy);
    }

    public Call<ApiResponse<List<ProductStatisticResponse>>> getTopProducts(String token, Instant from,
            Instant to, Integer limit, String sortBy) {
        return statisticApi.getTopProducts(formatToken(token), from, to, limit, sortBy);
    }

    public Call<ApiResponse<List<CatalogStatisticResponse>>> getByCatalog(String token, Instant from,
            Instant to) {
        return statisticApi.getByCatalog(formatToken(token), from, to);
    }

    public Call<ApiResponse<List<StaffStatisticResponse>>> getByStaff(String token, Instant from, Instant to) {
        return statisticApi.getByStaff(formatToken(token), from, to);
    }

    public Call<ApiResponse<List<StatusStatisticResponse>>> getStatusDistribution(String token, Instant from,
            Instant to) {
        return statisticApi.getStatusDistribution(formatToken(token), from, to);
    }
}
