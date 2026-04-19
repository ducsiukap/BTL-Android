package com.example.ddht.data.remote.dto;

public class ChatRequest {
    private String message;

    public ChatRequest(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
