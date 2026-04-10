package com.example.app_be.service;

import com.example.app_be.controller.dto.request.CreateSaleOffRequest;
import com.example.app_be.controller.dto.response.SaleOffResponse;

import java.util.List;

public interface SaleOffService {
    public List<SaleOffResponse> getAllActiveSaleOff();

    public SaleOffResponse updateSaleOff(Long id, CreateSaleOffRequest request);
}
