package com.example.ddht.ui.manager.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ddht.R;
import com.example.ddht.data.remote.dto.CatalogStatisticResponse;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StatisticCatalogAdapter extends RecyclerView.Adapter<StatisticCatalogAdapter.CatalogViewHolder> {

     private List<CatalogStatisticResponse> catalogList = new ArrayList<>();

     public void setCatalogList(List<CatalogStatisticResponse> catalogList) {
          this.catalogList = catalogList;
          notifyDataSetChanged();
     }

     @NonNull
     @Override
     public CatalogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
          View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_statistic_catalog, parent, false);
          return new CatalogViewHolder(view);
     }

     @Override
     public void onBindViewHolder(@NonNull CatalogViewHolder holder, int position) {
          CatalogStatisticResponse catalog = catalogList.get(position);
          holder.bind(catalog);
     }

     @Override
     public int getItemCount() {
          return catalogList != null ? catalogList.size() : 0;
     }

     static class CatalogViewHolder extends RecyclerView.ViewHolder {
          private final TextView tvCatalogName;
          private final TextView tvSoldQuantity;
          private final TextView tvRevenue;

          public CatalogViewHolder(@NonNull View itemView) {
               super(itemView);
               tvCatalogName = itemView.findViewById(R.id.tvCatalogName);
               tvSoldQuantity = itemView.findViewById(R.id.tvSoldQuantity);
               tvRevenue = itemView.findViewById(R.id.tvRevenue);
          }

          public void bind(CatalogStatisticResponse catalog) {
               tvCatalogName.setText(catalog.getCatalogName());
               tvSoldQuantity.setText("Đã bán: " + catalog.getSoldQuantity());

               NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
               if (catalog.getRevenue() != null) {
                    tvRevenue.setText(format.format(catalog.getRevenue()));
               } else {
                    tvRevenue.setText("0 đ");
               }
          }
     }
}
