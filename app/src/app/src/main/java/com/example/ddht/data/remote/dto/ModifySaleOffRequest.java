package com.example.ddht.data.remote.dto;

public class ModifySaleOffRequest {
    private Double discount;
    private String startDate;
    private String endDate;
    private Boolean isActive;

    public ModifySaleOffRequest(Double discount, String startDate, String endDate, Boolean isActive) {
        this.discount = discount;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isActive = isActive;
    }

    public Double getDiscount() {
        return discount;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public Boolean getActive() {
        return isActive;
    }
}
