package com.example.ddht.data.repository;

import com.example.ddht.data.remote.NetworkClient;
import com.example.ddht.data.remote.api.ChatApi;
import com.example.ddht.data.remote.dto.ApiResponse;
import com.example.ddht.data.remote.dto.ChatRequest;
import com.example.ddht.data.remote.dto.ChatResponse;

import retrofit2.Call;

public class ChatRepository {
    private final ChatApi chatApi;

    public ChatRepository() {
        chatApi = NetworkClient.getRetrofit().create(ChatApi.class);
    }

    public Call<ApiResponse<ChatResponse>> sendMessage(String message) {
        return chatApi.sendMessage(new ChatRequest(message));
    }
}
