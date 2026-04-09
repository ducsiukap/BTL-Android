package com.example.ddht.data.model;

public class Product {
    private final String name;
    private final String subtitle;
    private final double price;

    public Product(String name, String subtitle, double price) {
        this.name = name;
        this.subtitle = subtitle;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public double getPrice() {
        return price;
    }
}
