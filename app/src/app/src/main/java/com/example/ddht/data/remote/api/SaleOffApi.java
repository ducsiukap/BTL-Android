package com.example.ddht.data.remote.api;

import com.example.ddht.data.remote.dto.ApiResponse;
import com.example.ddht.data.remote.dto.ModifySaleOffRequest;
import com.example.ddht.data.remote.dto.SaleOffDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Body;

public interface SaleOffApi {
    @GET("saleoffs")
    Call<ApiResponse<List<SaleOffDto>>> getAllSaleOffs();

    @PUT("saleoffs/{id}")
    Call<ApiResponse<SaleOffDto>> updateSaleOff(@Path("id") Long saleOffId,
                                                @Body ModifySaleOffRequest request,
                                                @Header("Authorization") String bearerToken);
}
