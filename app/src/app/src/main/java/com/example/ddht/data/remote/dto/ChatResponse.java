package com.example.ddht.data.remote.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ChatResponse {
    @SerializedName("response")
    private String response;

    @SerializedName("session_id")
    private String sessionId;

    public String getResponse() {
        return response;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}
