package com.example.ddht.ui.common.adapter;

import android.graphics.Paint;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ddht.R;
import com.example.ddht.data.manager.CartManager;
import com.example.ddht.data.model.Product;
import com.bumptech.glide.Glide;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    private final List<Product> products = new ArrayList<>();
    private OnProductClickListener listener;

    public ProductAdapter(List<Product> products) {
        submit(products);
    }

    public ProductAdapter(List<Product> products, OnProductClickListener listener) {
        this.listener = listener;
        submit(products);
    }

    public void setOnProductClickListener(OnProductClickListener listener) {
        this.listener = listener;
    }

    public void submit(List<Product> items) {
        products.clear();
        if (items != null) {
            products.addAll(items);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = products.get(position);
        holder.tvName.setText(product.getName());
        holder.tvSubtitle.setText(product.getSubtitle());
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        holder.tvPrice.setText(formatter.format(product.getDisplayPrice()));

        if (product.isSaleOff() && product.getOriginalPrice() > product.getDisplayPrice()) {
            holder.tvSaleOff.setVisibility(View.VISIBLE);
            holder.tvOriginalPrice.setVisibility(View.VISIBLE);
            holder.tvOriginalPrice.setText(formatter.format(product.getOriginalPrice()));
            holder.tvOriginalPrice.setPaintFlags(holder.tvOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.tvPrice.setTextColor(holder.itemView.getContext().getColor(R.color.brand_error));
            holder.tvPrice.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20); // Reduced size slightly to fit button
        } else {
            holder.tvSaleOff.setVisibility(View.GONE);
            holder.tvOriginalPrice.setVisibility(View.GONE);
            holder.tvPrice.setTextColor(holder.itemView.getContext().getColor(R.color.brand_text_primary));
            holder.tvPrice.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        }

        Glide.with(holder.itemView.getContext())
                .load(product.getImageUrl())
                .placeholder(R.drawable.product_image_placeholder)
                .error(R.drawable.product_image_placeholder)
                .into(holder.ivProductImage);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onProductClick(product);
            }
        });

        holder.btnAddToCart.setOnClickListener(v -> {
            CartManager.getInstance().addProduct(product, 1);
            android.widget.Toast.makeText(holder.itemView.getContext(), 
                "Đã thêm " + product.getName() + " vào giỏ hàng", 
                android.widget.Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvSubtitle;
        TextView tvPrice;
        TextView tvOriginalPrice;
        TextView tvSaleOff;
        ImageView ivProductImage;
        ImageButton btnAddToCart;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvProductName);
            tvSubtitle = itemView.findViewById(R.id.tvProductSubtitle);
            tvPrice = itemView.findViewById(R.id.tvProductPrice);
            tvOriginalPrice = itemView.findViewById(R.id.tvProductOriginalPrice);
            tvSaleOff = itemView.findViewById(R.id.tvProductSaleOff);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            btnAddToCart = itemView.findViewById(R.id.btnAddToCart);
        }
    }
}
