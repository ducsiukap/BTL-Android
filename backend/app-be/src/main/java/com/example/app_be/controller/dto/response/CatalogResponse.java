package com.example.app_be.controller.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Builder
public record CatalogResponse(
        long id, String name
) {
}
