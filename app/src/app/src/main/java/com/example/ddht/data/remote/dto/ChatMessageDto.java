package com.example.ddht.data.remote.dto;

public class ChatMessageDto {
    private String text;
    private boolean isUser;
    private Long timestamp;

    public ChatMessageDto(String text, boolean isUser) {
        this.text = text;
        this.isUser = isUser;
        this.timestamp = System.currentTimeMillis();
    }

    public String getText() { return text; }
    public boolean isUser() { return isUser; }
    public Long getTimestamp() { return timestamp; }
}
