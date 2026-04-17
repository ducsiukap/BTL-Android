package com.example.ddht.data.manager;

import com.example.ddht.data.model.CartItem;
import com.example.ddht.data.model.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CartManager {
    private static CartManager instance;
    private final List<CartItem> cartItems;

    private CartManager() {
        cartItems = new ArrayList<>();
    }

    public static synchronized CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    public void addProduct(Product product, int quantity) {
        if (product == null || product.getId() == null) return;

        for (CartItem item : cartItems) {
            if (Objects.equals(item.getProduct().getId(), product.getId())) {
                item.setQuantity(item.getQuantity() + quantity);
                return;
            }
        }
        cartItems.add(new CartItem(product, quantity));
    }

    public List<CartItem> getCartItems() {
        return new ArrayList<>(cartItems);
    }

    public void removeProduct(Long productId) {
        cartItems.removeIf(item -> Objects.equals(item.getProduct().getId(), productId));
    }

    public void updateQuantity(Long productId, int quantity) {
        for (CartItem item : cartItems) {
            if (Objects.equals(item.getProduct().getId(), productId)) {
                item.setQuantity(quantity);
                if (item.getQuantity() <= 0) {
                    removeProduct(productId);
                }
                return;
            }
        }
    }

    public double getTotalPrice() {
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getSubtotal();
        }
        return total;
    }

    public void clearCart() {
        cartItems.clear();
    }

    public int getItemCount() {
        int count = 0;
        for (CartItem item : cartItems) {
            count += item.getQuantity();
        }
        return count;
    }
}
