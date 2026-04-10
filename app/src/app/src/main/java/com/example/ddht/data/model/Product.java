package com.example.ddht.data.model;

public class Product {
    private final String name;
    private final String subtitle;
    private final double displayPrice;
    private final double originalPrice;
    private final boolean saleOff;
    private final String imageUrl;

    public Product(String name, String subtitle, double displayPrice, double originalPrice, boolean saleOff, String imageUrl) {
        this.name = name;
        this.subtitle = subtitle;
        this.displayPrice = displayPrice;
        this.originalPrice = originalPrice;
        this.saleOff = saleOff;
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public double getDisplayPrice() {
        return displayPrice;
    }

    public double getOriginalPrice() {
        return originalPrice;
    }

    public boolean isSaleOff() {
        return saleOff;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
