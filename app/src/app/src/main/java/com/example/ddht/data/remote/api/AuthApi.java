package com.example.ddht.data.remote.api;

import com.example.ddht.data.remote.dto.ApiResponse;
import com.example.ddht.data.remote.dto.LoginRequest;
import com.example.ddht.data.remote.dto.LoginResponseData;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApi {
    @POST("auth/login")
    Call<ApiResponse<LoginResponseData>> login(@Body LoginRequest request);
}
