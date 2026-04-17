package com.example.ddht.data.remote.dto;

import java.util.List;

public class ChatResponse {
    private String response;
    private List<Action> actions;
    private List<ProductDto> products; // Danh sách sản phẩm Bot tìm được

    public static class Action {
        private String type; // "ADD_TO_CART", "SEARCH", "CLEAR_FILTER", "SHOW_DETAILS"
        private Long productId;
        private Integer quantity;
        private String searchQuery;
        private Long catalogId;

        public String getType() { return type; }
        public Long getProductId() { return productId; }
        public Integer getQuantity() { return quantity; }
        public String getSearchQuery() { return searchQuery; }
        public Long getCatalogId() { return catalogId; }
    }

    public String getResponse() { return response; }
    public List<Action> getActions() { return actions; }
    public List<ProductDto> getProducts() { return products; }
}
