package com.example.ddht.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class VoiceChatResponse {
    @SerializedName("original_text")
    private String originalText;

    @SerializedName("corrected_text")
    private String correctedText;

    private String response;

    @SerializedName("session_id")
    private String sessionId;

    public String getOriginalText() {
        return originalText;
    }

    public String getCorrectedText() {
        return correctedText;
    }

    public String getResponse() {
        return response;
    }

    public String getSessionId() {
        return sessionId;
    }
}