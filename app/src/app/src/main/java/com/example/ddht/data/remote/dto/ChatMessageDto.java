package com.example.ddht.data.remote.dto;

import java.util.List;

public class ChatMessageDto {
    private String text;
    private boolean isUser;
    private Long timestamp;
    private List<ProductDto> products; // Sản phẩm đi kèm tin nhắn

    public ChatMessageDto(String text, boolean isUser) {
        this(text, isUser, null);
    }

    public ChatMessageDto(String text, boolean isUser, List<ProductDto> products) {
        this.text = text;
        this.isUser = isUser;
        this.products = products;
        this.timestamp = System.currentTimeMillis();
    }

    public String getText() { return text; }
    public boolean isUser() { return isUser; }
    public Long getTimestamp() { return timestamp; }
    public List<ProductDto> getProducts() { return products; }
}
