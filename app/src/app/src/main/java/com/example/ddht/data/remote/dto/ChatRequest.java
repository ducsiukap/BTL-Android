package com.example.ddht.data.remote.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ChatRequest {
    private String message;
    @SerializedName("session_id")
    private String sessionId;
    @SerializedName("current_cart")
    private List<ChatCartItemDto> currentCart;

    public ChatRequest(String message) {
        this(message, null, null);
    }

    public ChatRequest(String message, String sessionId) {
        this(message, sessionId, null);
    }

    public ChatRequest(String message, String sessionId, List<ChatCartItemDto> currentCart) {
        this.message = message;
        this.sessionId = sessionId;
        this.currentCart = currentCart;
    }

    public String getMessage() {
        return message;
    }

    public String getSessionId() {
        return sessionId;
    }

    public List<ChatCartItemDto> getCurrentCart() {
        return currentCart;
    }
}
