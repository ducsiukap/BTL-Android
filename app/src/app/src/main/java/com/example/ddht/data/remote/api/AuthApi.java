package com.example.ddht.data.remote.api;

import com.example.ddht.data.remote.dto.ApiResponse;
import com.example.ddht.data.remote.dto.ChangePasswordRequest;
import com.example.ddht.data.remote.dto.LoginRequest;
import com.example.ddht.data.remote.dto.LoginResponseData;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface AuthApi {
    @POST("auth/login")
    Call<ApiResponse<LoginResponseData>> login(@Body LoginRequest request);

    @POST("auth/reset-password/{id}")
    Call<ApiResponse<Void>> resetPassword(@Path("id") String userId,
                                          @Header("Authorization") String bearerToken);

    @POST("auth/change-password")
    Call<ApiResponse<Void>> changePassword(@Body ChangePasswordRequest request,
                                           @Header("Authorization") String bearerToken);
}
