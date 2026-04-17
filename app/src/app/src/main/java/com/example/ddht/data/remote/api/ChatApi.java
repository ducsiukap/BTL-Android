package com.example.ddht.data.remote.api;

import com.example.ddht.data.remote.dto.ApiResponse;
import com.example.ddht.data.remote.dto.ChatRequest;
import com.example.ddht.data.remote.dto.ChatResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ChatApi {
    @POST("chat")
    Call<ApiResponse<ChatResponse>> sendMessage(@Body ChatRequest request);
}
