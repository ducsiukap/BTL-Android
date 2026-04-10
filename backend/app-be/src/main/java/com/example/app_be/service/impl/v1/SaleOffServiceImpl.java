package com.example.app_be.service.impl.v1;

import com.example.app_be.controller.dto.request.CreateSaleOffRequest;
import com.example.app_be.controller.dto.response.SaleOffResponse;
import com.example.app_be.core.exception.ResourceNotFoundException;
import com.example.app_be.model.Product;
import com.example.app_be.model.SaleOff;
import com.example.app_be.repository.SaleOffRepository;
import com.example.app_be.service.SaleOffService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class SaleOffServiceImpl implements SaleOffService {
    private final SaleOffRepository saleOffRepository;

    @Override
    public List<SaleOffResponse> getAllActiveSaleOff() {
        List<SaleOff> saleOffs = saleOffRepository.findAllActiveSaleOffs();
        return saleOffs.stream()
                .map(saleOff -> new SaleOffResponse(
                        saleOff.getId(), saleOff.getDiscount(),
                        saleOff.getStartDate(), saleOff.getEndDate(),
                        saleOff.isActive(), saleOff.getProduct().getId(), saleOff.getProduct().getName()
                ))
                .toList();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public SaleOffResponse updateSaleOff(Long id, CreateSaleOffRequest request) {
        SaleOff saleOff = saleOffRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("SaleOff not found!")
        );

        Product product = saleOff.getProduct();
        if (request.discount().compareTo(product.getPrice()) > 0) {
            throw new IllegalArgumentException("Discount cannot be greater than product price!");
        }

//        System.out.println(request.isActive());
        saleOff.setDiscount(request.discount());
        saleOff.setStartDate(request.startDate());
        saleOff.setEndDate(request.endDate());
        saleOff.setActive(request.isActive());

        return new SaleOffResponse(
                saleOff.getId(), saleOff.getDiscount(),
                saleOff.getStartDate(), saleOff.getEndDate(),
                saleOff.isActive(), product.getId(), product.getName()
        );
    }
}
