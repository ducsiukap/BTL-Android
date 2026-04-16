package com.example.ddht.ui.common.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ddht.R;
import com.example.ddht.data.manager.CartManager;
import com.example.ddht.data.model.CartItem;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    private final List<CartItem> items = new ArrayList<>();
    private final OnCartChangeListener listener;

    public interface OnCartChangeListener {
        void onCartChanged();
    }

    public CartAdapter(OnCartChangeListener listener) {
        this.listener = listener;
        updateItems();
    }

    public void updateItems() {
        items.clear();
        items.addAll(CartManager.getInstance().getCartItems());
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = items.get(position);
        holder.tvName.setText(item.getProduct().getName());
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        holder.tvPrice.setText(formatter.format(item.getSubtotal()));
        holder.tvQty.setText(String.valueOf(item.getQuantity()));

        Glide.with(holder.itemView.getContext())
                .load(item.getProduct().getImageUrl())
                .placeholder(R.drawable.product_image_placeholder)
                .error(R.drawable.product_image_placeholder)
                .into(holder.ivImage);

        holder.btnPlus.setOnClickListener(v -> {
            CartManager.getInstance().updateQuantity(item.getProduct().getId(), item.getQuantity() + 1);
            updateItems();
            if (listener != null) listener.onCartChanged();
        });

        holder.btnMinus.setOnClickListener(v -> {
            if (item.getQuantity() > 1) {
                CartManager.getInstance().updateQuantity(item.getProduct().getId(), item.getQuantity() - 1);
                updateItems();
                if (listener != null) listener.onCartChanged();
            }
        });

        holder.btnRemove.setOnClickListener(v -> {
            CartManager.getInstance().removeProduct(item.getProduct().getId());
            updateItems();
            if (listener != null) listener.onCartChanged();
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvName, tvPrice, tvQty;
        ImageButton btnPlus, btnMinus, btnRemove;

        public CartViewHolder(@NonNull View view) {
            super(view);
            ivImage = view.findViewById(R.id.ivCartItemImage);
            tvName = view.findViewById(R.id.tvCartItemName);
            tvPrice = view.findViewById(R.id.tvCartItemPrice);
            tvQty = view.findViewById(R.id.tvCartItemQty);
            btnPlus = view.findViewById(R.id.btnCartItemPlus);
            btnMinus = view.findViewById(R.id.btnCartItemMinus);
            btnRemove = view.findViewById(R.id.btnCartItemRemove);
        }
    }
}
