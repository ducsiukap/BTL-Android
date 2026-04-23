package com.example.ddht.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class ChatRequest {
    private String message;
    @SerializedName("session_id")
    private String sessionId;

    public ChatRequest(String message) {
        this(message, null);
    }

    public ChatRequest(String message, String sessionId) {
        this.message = message;
        this.sessionId = sessionId;
    }

    public String getMessage() {
        return message;
    }

    public String getSessionId() {
        return sessionId;
    }
}
