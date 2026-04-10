package com.example.ddht.ui.manager.adapter;

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
import com.example.ddht.data.remote.dto.ProductImageDto;

import java.util.ArrayList;
import java.util.List;

public class ManageProductImageAdapter extends RecyclerView.Adapter<ManageProductImageAdapter.ImageViewHolder> {
    public interface ManageImageActionListener {
        void onDelete(ProductImageDto image);
    }

    private final List<ProductImageDto> images = new ArrayList<>();
    private final ManageImageActionListener listener;

    public ManageProductImageAdapter(ManageImageActionListener listener) {
        this.listener = listener;
    }

    public void submit(List<ProductImageDto> items) {
        images.clear();
        if (items != null) {
            images.addAll(items);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_manage_product_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        ProductImageDto item = images.get(position);
        holder.tvLabel.setText(holder.itemView.getContext().getString(R.string.manager_product_image_item_label, position + 1));
        Glide.with(holder.itemView.getContext())
                .load(item.getUrl())
                .placeholder(R.drawable.product_image_placeholder)
                .error(R.drawable.product_image_placeholder)
                .into(holder.ivImage);

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDelete(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvLabel;
        ImageButton btnDelete;

        ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivManageProductImage);
            tvLabel = itemView.findViewById(R.id.tvManageProductImageLabel);
            btnDelete = itemView.findViewById(R.id.btnManageProductImageDelete);
        }
    }
}
