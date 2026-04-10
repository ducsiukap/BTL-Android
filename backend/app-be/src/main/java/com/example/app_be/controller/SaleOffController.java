package com.example.app_be.controller;

import com.example.app_be.controller.dto.request.CreateSaleOffRequest;
import com.example.app_be.controller.dto.response.ApiResponse;
import com.example.app_be.controller.dto.response.SaleOffResponse;
import com.example.app_be.core.annotation.ApiV1;
import com.example.app_be.service.SaleOffService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@ApiV1
@Controller
@RequestMapping("/saleoffs")
@RequiredArgsConstructor
public class SaleOffController {

    private final SaleOffService saleOffService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SaleOffResponse>>> getAllSaleOff() {
        List<SaleOffResponse> saleOffs = saleOffService.getAllActiveSaleOff();
        return ResponseEntity.ok(ApiResponse.success(saleOffs));
    }

    @PostMapping("/{id}")
    public ResponseEntity<ApiResponse<SaleOffResponse>> updateSaleOff(
            @PathVariable Long id,
            @Valid @RequestBody CreateSaleOffRequest request
    ) {

        SaleOffResponse saleOff = saleOffService.updateSaleOff(id, request);
        return ResponseEntity.ok(ApiResponse.success(saleOff));

    }
}
