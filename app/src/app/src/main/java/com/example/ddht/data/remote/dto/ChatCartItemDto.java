package com.example.ddht.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class ChatCartItemDto {
     @SerializedName("item_id")
     private String itemId;

     @SerializedName("name")
     private String name;

     @SerializedName("price")
     private double price;

     @SerializedName("quantity")
     private int quantity;

     @SerializedName("url")
     private String url;

     public ChatCartItemDto() {
     }

     public ChatCartItemDto(String itemId, String name, double price, int quantity, String url) {
          this.itemId = itemId;
          this.name = name;
          this.price = price;
          this.quantity = quantity;
          this.url = url;
     }

     public String getItemId() {
          return itemId;
     }

     public String getName() {
          return name;
     }

     public double getPrice() {
          return price;
     }

     public int getQuantity() {
          return quantity;
     }

     public String getUrl() {
          return url;
     }
}
