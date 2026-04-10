package com.example.app_be.controller.dto.response;

import lombok.Builder;

@Builder
public record ProductImageResponse(
        long id, String url
) {
}
