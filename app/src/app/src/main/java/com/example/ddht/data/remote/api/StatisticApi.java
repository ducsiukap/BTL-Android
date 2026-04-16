package com.example.ddht.data.remote.api;

import com.example.ddht.data.remote.dto.ApiResponse;
import com.example.ddht.data.remote.dto.CatalogStatisticResponse;
import com.example.ddht.data.remote.dto.ProductStatisticResponse;
import com.example.ddht.data.remote.dto.StatisticOverviewResponse;
import com.example.ddht.data.remote.dto.StatusStatisticResponse;
import com.example.ddht.data.remote.dto.TimeSeriesPointResponse;

import java.time.Instant;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface StatisticApi {
    @GET("/statistics/overview")
    Call<ApiResponse<StatisticOverviewResponse>> getStatisticOverview(
            @Header("Authentication") String bearerToken,
            @Query("from")Instant from,
            @Query("to") Instant to
    );

    @GET("/statistics/revenue-series")
    Call<ApiResponse<List<TimeSeriesPointResponse>>> getRevenueSeries(
            @Header("Authentication") String bearerToken,
            @Query("from") Instant from,
            @Query("to") Instant to,
            @Query("groupBy") String groupBy
    );

    @GET("/statistics/order-series")
    Call<ApiResponse<List<TimeSeriesPointResponse>>> getOrderSeries(
            @Header("Authentication") String bearerToken,
            @Query("from") Instant from,
            @Query("to") Instant to,
            @Query("groupBy") String groupBy
    );

    @GET("/statistics/top-products")
    Call<ApiResponse<List<ProductStatisticResponse>>> getTopProducts (
            @Header("Authentication") String bearerToken,
            @Query("from") Instant from,
            @Query("to") Instant to,
            @Query("limit") Integer limit
    );

    @GET("/statistics/by-catalog")
    Call<ApiResponse<List<CatalogStatisticResponse>>> getByCatalog(
            @Header("Authentication") String bearerToken,
            @Query("from") Instant from,
            @Query("to") Instant to
    );

    @GET("/statistics/by-staff")
    Call<ApiResponse<List<CatalogStatisticResponse>>> getByStaff(
            @Header("Authentication") String bearerToken,
            @Query("from") Instant from,
            @Query("to") Instant to
    );

    @GET("/statistic/status-distribution")
    Call<ApiResponse<List<StatusStatisticResponse>>> getStatusDistribution(
            @Header("Authentication") String bearerToken,
            @Query("from") Instant from,
            @Query("to") Instant to
    );
}
