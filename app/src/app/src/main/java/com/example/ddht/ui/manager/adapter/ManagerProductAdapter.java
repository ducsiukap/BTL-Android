package com.example.ddht.ui.manager.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ddht.R;
import com.example.ddht.data.remote.dto.ProductDto;
import com.example.ddht.data.remote.dto.ProductImageDto;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ManagerProductAdapter extends RecyclerView.Adapter<ManagerProductAdapter.ProductViewHolder> {
    public interface ProductActionListener {
        void onOpenDetail(ProductDto product);
        void onToggleSelling(ProductDto product, boolean isSelling);
    }

    private final List<ProductDto> products = new ArrayList<>();
    private final ProductActionListener listener;

    public ManagerProductAdapter(ProductActionListener listener) {
        this.listener = listener;
    }

    public void submit(List<ProductDto> items) {
        products.clear();
        if (items != null) {
            products.addAll(items);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_manager_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        ProductDto product = products.get(position);
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        String name = product.getName() == null || product.getName().trim().isEmpty()
                ? holder.itemView.getContext().getString(R.string.manager_product_default_name)
                : product.getName();
        holder.tvName.setText(name);

        double originalPrice = product.getOriginalPrice() == null ? 0 : product.getOriginalPrice();
        double discountedPrice = product.getDiscountedPrice() == null ? originalPrice : product.getDiscountedPrice();

        holder.tvOriginalPrice.setText(holder.itemView.getContext().getString(
                R.string.manager_product_original_price,
                formatter.format(originalPrice)
        ));
        holder.tvDiscountedPrice.setText(holder.itemView.getContext().getString(
                R.string.manager_product_discounted_price,
                formatter.format(discountedPrice)
        ));

        boolean hasSale = Boolean.TRUE.equals(product.getSaleOff()) && discountedPrice < originalPrice;
        if (hasSale) {
            holder.tvDiscountedPrice.setTextColor(holder.itemView.getContext().getColor(R.color.brand_error));
        } else {
            holder.tvDiscountedPrice.setTextColor(holder.itemView.getContext().getColor(R.color.brand_text_primary));
        }

        holder.switchSelling.setOnCheckedChangeListener(null);
        boolean isSelling = product.getSelling() == null || product.getSelling();
        holder.tvStatus.setText(isSelling
                ? R.string.manager_product_status_selling
                : R.string.manager_product_status_stopped);
        holder.tvStatus.setTextColor(holder.itemView.getContext().getColor(
                isSelling ? R.color.brand_primary : R.color.brand_error
        ));
        holder.switchSelling.setChecked(isSelling);
        holder.switchSelling.setOnCheckedChangeListener((buttonView, checked) -> {
            if (listener != null) {
                listener.onToggleSelling(product, checked);
            }
        });

        Glide.with(holder.itemView.getContext())
                .load(resolveImageUrl(product))
                .placeholder(R.drawable.product_image_placeholder)
                .error(R.drawable.product_image_placeholder)
                .into(holder.ivImage);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onOpenDetail(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    private String resolveImageUrl(ProductDto product) {
        List<ProductImageDto> images = product.getImages();
        if (images == null || images.isEmpty() || images.get(0) == null) {
            return null;
        }
        return images.get(0).getUrl();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvName;
        TextView tvOriginalPrice;
        TextView tvDiscountedPrice;
        TextView tvStatus;
        SwitchCompat switchSelling;

        ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivManagerProductImage);
            tvName = itemView.findViewById(R.id.tvManagerProductName);
            tvOriginalPrice = itemView.findViewById(R.id.tvManagerProductOriginalPrice);
            tvDiscountedPrice = itemView.findViewById(R.id.tvManagerProductDiscountedPrice);
            tvStatus = itemView.findViewById(R.id.tvManagerProductStatus);
            switchSelling = itemView.findViewById(R.id.switchManagerProductSelling);
        }
    }
}
