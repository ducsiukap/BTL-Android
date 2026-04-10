package com.example.ddht.ui.manager.adapter;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ddht.R;

import java.util.ArrayList;
import java.util.List;

public class SelectedImageUriAdapter extends RecyclerView.Adapter<SelectedImageUriAdapter.ImageViewHolder> {
    public interface SelectedImageActionListener {
        void onRemove(int position);
    }

    private final List<Uri> uris = new ArrayList<>();
    private final SelectedImageActionListener listener;

    public SelectedImageUriAdapter(SelectedImageActionListener listener) {
        this.listener = listener;
    }

    public void submit(List<Uri> items) {
        uris.clear();
        if (items != null) {
            uris.addAll(items);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_selected_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Uri uri = uris.get(position);
        Glide.with(holder.itemView.getContext())
                .load(uri)
                .placeholder(R.drawable.product_image_placeholder)
                .error(R.drawable.product_image_placeholder)
                .into(holder.imageView);

        holder.btnRemove.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRemove(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return uris.size();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageButton btnRemove;

        ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.ivSelectedImage);
            btnRemove = itemView.findViewById(R.id.btnRemoveSelectedImage);
        }
    }
}
