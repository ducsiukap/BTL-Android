package com.example.ddht.data.repository;

import com.example.ddht.data.remote.NetworkClient;
import com.example.ddht.data.remote.api.AuthApi;
import com.example.ddht.data.remote.api.UserApi;
import com.example.ddht.data.remote.dto.ApiResponse;
import com.example.ddht.data.remote.dto.LoginRequest;
import com.example.ddht.data.remote.dto.LoginResponseData;
import com.example.ddht.data.remote.dto.UserDto;

import retrofit2.Call;

public class AuthRepository {
    private final AuthApi authApi;
    private final UserApi userApi;

    public AuthRepository() {
        authApi = NetworkClient.getRetrofit().create(AuthApi.class);
        userApi = NetworkClient.getRetrofit().create(UserApi.class);
    }

    public Call<ApiResponse<LoginResponseData>> login(String email, String password) {
        return authApi.login(new LoginRequest(email, password));
    }

    public Call<ApiResponse<UserDto>> getMe(String accessToken) {
        return userApi.getMe("Bearer " + accessToken);
    }
}
