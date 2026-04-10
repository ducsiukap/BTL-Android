package com.example.ddht.data.repository;

import com.example.ddht.data.remote.NetworkClient;
import com.example.ddht.data.remote.api.SaleOffApi;
import com.example.ddht.data.remote.dto.ApiResponse;
import com.example.ddht.data.remote.dto.ModifySaleOffRequest;
import com.example.ddht.data.remote.dto.SaleOffDto;

import java.util.List;

import retrofit2.Call;

public class SaleOffRepository {
    private final SaleOffApi saleOffApi;

    public SaleOffRepository() {
        saleOffApi = NetworkClient.getRetrofit().create(SaleOffApi.class);
    }

    public Call<ApiResponse<List<SaleOffDto>>> getAllSaleOffs() {
        return saleOffApi.getAllSaleOffs();
    }

    public Call<ApiResponse<SaleOffDto>> updateSaleOff(Long saleOffId,
                                                        Double discount,
                                                        String startDate,
                                                        String endDate,
                                                        Boolean isActive,
                                                        String accessToken) {
        ModifySaleOffRequest request = new ModifySaleOffRequest(discount, startDate, endDate, isActive);
        return saleOffApi.updateSaleOff(saleOffId, request, "Bearer " + accessToken);
    }
}
