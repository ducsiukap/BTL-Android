package com.example.app_be.controller.dto.request;

import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public record AddImageRequest (
        @NotNull(message = "Images is required")
        List<MultipartFile> images
) {
}
