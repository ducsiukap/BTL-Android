package com.example.ddht.data.repository;

import com.example.ddht.data.remote.NetworkClient;
import com.example.ddht.data.remote.api.AuthApi;
import com.example.ddht.data.remote.api.UserApi;
import com.example.ddht.data.remote.dto.ApiResponse;
import com.example.ddht.data.remote.dto.ChangePasswordRequest;
import com.example.ddht.data.remote.dto.CreateUserRequest;
import com.example.ddht.data.remote.dto.LoginRequest;
import com.example.ddht.data.remote.dto.LoginResponseData;
import com.example.ddht.data.remote.dto.UpdateUserRequest;
import com.example.ddht.data.remote.dto.UserDto;

import java.util.List;

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

    public Call<ApiResponse<List<UserDto>>> searchUsers(String query, String accessToken) {
        return userApi.searchUsers(query == null ? "" : query, "Bearer " + accessToken);
    }

    public Call<ApiResponse<UserDto>> createUser(String fullName, String email, String role, String accessToken) {
        CreateUserRequest request = new CreateUserRequest(fullName, email, role);
        return userApi.createUser(request, "Bearer " + accessToken);
    }

    public Call<ApiResponse<UserDto>> updateUser(String userId, String fullName, String email, String accessToken) {
        UpdateUserRequest request = new UpdateUserRequest(fullName, email);
        return userApi.updateUser(userId, request, "Bearer " + accessToken);
    }

    public Call<ApiResponse<Void>> deleteUser(String userId, String accessToken) {
        return userApi.deleteUser(userId, "Bearer " + accessToken);
    }

    public Call<ApiResponse<Void>> resetPassword(String userId, String accessToken) {
        return authApi.resetPassword(userId, "Bearer " + accessToken);
    }

    public Call<ApiResponse<Void>> changePassword(String oldPassword, String newPassword, String accessToken) {
        ChangePasswordRequest request = new ChangePasswordRequest(oldPassword, newPassword);
        return authApi.changePassword(request, "Bearer " + accessToken);
    }
}
