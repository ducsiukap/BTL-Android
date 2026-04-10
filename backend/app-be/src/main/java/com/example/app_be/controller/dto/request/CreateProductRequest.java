package com.example.app_be.controller.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@AllArgsConstructor
@Builder
@NoArgsConstructor @Getter @Setter
public class CreateProductRequest {
        @NotBlank(message = "Product's name is required")
        String name;

//        @NotBlank(message = "Product's description is required")
        String description = "";

        @NotNull(message = "Product's price is required")
        @DecimalMin(value = "0", message = "Product's price must be greater than 0")
        BigDecimal price;

        Boolean isSelling = true;
        List<MultipartFile> images = List.of();
}
