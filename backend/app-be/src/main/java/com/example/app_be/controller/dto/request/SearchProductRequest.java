package com.example.app_be.controller.dto.request;

import lombok.Builder;

@Builder
public record SearchProductRequest(
        String query, Long catalogId,
        Integer page, Integer size
) {
    public SearchProductRequest {
        if (page == null) page = 0;
        if (size == null) size = 20;
    }
}
