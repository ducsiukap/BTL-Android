package com.example.ddht.data.remote.api;

import com.example.ddht.data.remote.dto.ApiResponse;
import com.example.ddht.data.remote.dto.CreateUserRequest;
import com.example.ddht.data.remote.dto.UpdateUserRequest;
import com.example.ddht.data.remote.dto.UserDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface UserApi {
    @GET("users/me")
    Call<ApiResponse<UserDto>> getMe(@Header("Authorization") String bearerToken);

    @POST("users")
    Call<ApiResponse<UserDto>> createUser(@Body CreateUserRequest request, @Header("Authorization") String bearerToken);

    @GET("users")
    Call<ApiResponse<List<UserDto>>> searchUsers(@Query("query") String query, @Header("Authorization") String bearerToken);

    @POST("users/{id}")
    Call<ApiResponse<UserDto>> updateUser(@Path("id") String userId, @Body UpdateUserRequest request, @Header("Authorization") String bearerToken);

    @DELETE("users/{id}")
    Call<ApiResponse<Void>> deleteUser(@Path("id") String userId, @Header("Authorization") String bearerToken);
}
