package com.example.ddht.ui.common.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ddht.R;
import com.example.ddht.data.manager.CartManager;
import com.example.ddht.data.model.Product;
import com.example.ddht.data.remote.dto.ChatMessageDto;
import com.example.ddht.data.remote.dto.ProductDto;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
    private final List<ChatMessageDto> messages = new ArrayList<>();

    public void addMessage(ChatMessageDto message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_message, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessageDto message = messages.get(position);
        
        if (message.isUser()) {
            holder.tvUser.setVisibility(View.VISIBLE);
            holder.tvBot.setVisibility(View.GONE);
            holder.layoutBotProducts.setVisibility(View.GONE);
            holder.tvUser.setText(message.getText());
        } else {
            holder.tvBot.setVisibility(View.VISIBLE);
            holder.tvUser.setVisibility(View.GONE);
            holder.tvBot.setText(message.getText());

            // Xử lý hiển thị danh sách sản phẩm của Bot
            if (message.getProducts() != null && !message.getProducts().isEmpty()) {
                holder.layoutBotProducts.setVisibility(View.VISIBLE);
                renderProducts(holder.layoutBotProducts, message.getProducts());
            } else {
                holder.layoutBotProducts.setVisibility(View.GONE);
                holder.layoutBotProducts.removeAllViews();
            }
        }
    }

    private void renderProducts(LinearLayout container, List<ProductDto> products) {
        container.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(container.getContext());
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        for (ProductDto dto : products) {
            View productView = inflater.inflate(R.layout.item_chat_product, container, false);
            ImageView ivImage = productView.findViewById(R.id.ivChatProductImage);
            TextView tvName = productView.findViewById(R.id.tvChatProductName);
            TextView tvPrice = productView.findViewById(R.id.tvChatProductPrice);
            ImageButton btnAdd = productView.findViewById(R.id.btnChatAddToCart);

            tvName.setText(dto.getName());
            double price = dto.getDiscountedPrice() != null ? dto.getDiscountedPrice() : (dto.getOriginalPrice() != null ? dto.getOriginalPrice() : 0);
            tvPrice.setText(format.format(price));

            if (dto.getImages() != null && !dto.getImages().isEmpty()) {
                Glide.with(container.getContext())
                     .load(dto.getImages().get(0).getUrl())
                     .placeholder(R.drawable.product_image_placeholder)
                     .into(ivImage);
            }

            btnAdd.setOnClickListener(v -> {
                Product model = new Product(dto.getId(), dto.getName(), dto.getDescription(), price, 
                        dto.getOriginalPrice() != null ? dto.getOriginalPrice() : price, 
                        Boolean.TRUE.equals(dto.getSaleOff()), 
                        (dto.getImages() != null && !dto.getImages().isEmpty()) ? dto.getImages().get(0).getUrl() : null);
                CartManager.getInstance().addProduct(model, 1);
                Toast.makeText(container.getContext(), "Đã thêm " + model.getName() + " vào giỏ hàng", Toast.LENGTH_SHORT).show();
            });

            container.addView(productView);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView tvBot, tvUser;
        LinearLayout layoutBotProducts;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBot = itemView.findViewById(R.id.tvMessageBot);
            tvUser = itemView.findViewById(R.id.tvMessageUser);
            layoutBotProducts = itemView.findViewById(R.id.layoutBotProducts);
        }
    }
}
