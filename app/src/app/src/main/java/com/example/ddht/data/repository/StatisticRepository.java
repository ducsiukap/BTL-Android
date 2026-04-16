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

    public Call<ApiResponse<StatisticOverviewResponse>> getStatisticOverview(String bearerToken, Instant from,
            Instant to) {
        return statisticApi.getStatisticOverview(bearerToken, from, to);
    }

    public Call<ApiResponse<List<TimeSeriesPointResponse>>> getRevenueSeries(String bearerToken, Instant from,
            Instant to, String groupBy) {
        return statisticApi.getRevenueSeries("Bearer " + bearerToken, from, to, groupBy);
    }

    public Call<ApiResponse<List<TimeSeriesPointResponse>>> getOrderSeries(String bearerToken, Instant from, Instant to,
            String groupBy) {
        return statisticApi.getOrderSeries(bearerToken, from, to, groupBy);
    }

    public Call<ApiResponse<List<ProductStatisticResponse>>> getTopProducts(String bearerToken, Instant from,
            Instant to, Integer limit) {
        return statisticApi.getTopProducts(bearerToken, from, to, limit);
    }

    public Call<ApiResponse<List<CatalogStatisticResponse>>> getByCatalog(String bearerToken, Instant from,
            Instant to) {
        return statisticApi.getByCatalog(bearerToken, from, to);
    }

    public Call<ApiResponse<List<StaffStatisticResponse>>> getByStaff(String bearerToken, Instant from, Instant to) {
        return statisticApi.getByStaff(bearerToken, from, to);
    }

    public Call<ApiResponse<List<StatusStatisticResponse>>> getStatusDistribution(String bearerToken, Instant from,
            Instant to) {
        return statisticApi.getStatusDistribution(bearerToken, from, to);
    }
}
