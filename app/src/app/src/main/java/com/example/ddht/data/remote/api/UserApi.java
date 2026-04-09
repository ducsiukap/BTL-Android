package com.example.ddht.data.remote.api;

import com.example.ddht.data.remote.dto.ApiResponse;
import com.example.ddht.data.remote.dto.UserDto;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface UserApi {
    @GET("users/me")
    Call<ApiResponse<UserDto>> getMe(@Header("Authorization") String bearerToken);
}
