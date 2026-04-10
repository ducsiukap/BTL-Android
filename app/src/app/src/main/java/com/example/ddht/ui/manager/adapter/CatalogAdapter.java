package com.example.ddht.ui.manager.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ddht.R;
import com.example.ddht.data.remote.dto.CatalogDto;

import java.util.ArrayList;
import java.util.List;

public class CatalogAdapter extends RecyclerView.Adapter<CatalogAdapter.CatalogViewHolder> {
    public interface CatalogActionListener {
        void onEdit(CatalogDto catalog);

        void onDelete(CatalogDto catalog);
    }

    private final List<CatalogDto> catalogs = new ArrayList<>();
    private final CatalogActionListener listener;

    public CatalogAdapter(CatalogActionListener listener) {
        this.listener = listener;
    }

    public void submit(List<CatalogDto> items) {
        catalogs.clear();
        if (items != null) {
            catalogs.addAll(items);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CatalogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_manager_catalog, parent, false);
        return new CatalogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CatalogViewHolder holder, int position) {
        CatalogDto catalog = catalogs.get(position);
        holder.tvName.setText(catalog.getName());
        holder.btnEdit.setOnClickListener(v -> listener.onEdit(catalog));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(catalog));
    }

    @Override
    public int getItemCount() {
        return catalogs.size();
    }

    static class CatalogViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        ImageButton btnEdit;
        ImageButton btnDelete;

        CatalogViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvCatalogName);
            btnEdit = itemView.findViewById(R.id.btnEditCatalog);
            btnDelete = itemView.findViewById(R.id.btnDeleteCatalog);
        }
    }
}
