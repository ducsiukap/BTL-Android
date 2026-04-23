package com.example.ddht.data.remote.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ChatResponse {
    @SerializedName("response")
    private String response;

    @SerializedName("session_id")
    private String sessionId;

    @SerializedName("action")
    private String action;

    @SerializedName(value = "action_data", alternate = { "action_item" })
    private List<ChatCartItemDto> actionData;

    public String getResponse() {
        return response;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getAction() {
        return action;
    }

    public List<ChatCartItemDto> getActionData() {
        return actionData;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setActionData(List<ChatCartItemDto> actionData) {
        this.actionData = actionData;
    }
}
