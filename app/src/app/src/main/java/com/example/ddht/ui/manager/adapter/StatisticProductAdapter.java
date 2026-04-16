package com.example.ddht.ui.manager.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ddht.R;
import com.example.ddht.data.remote.dto.ProductStatisticResponse;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StatisticProductAdapter extends RecyclerView.Adapter<StatisticProductAdapter.ProductViewHolder> {

     private List<ProductStatisticResponse> productList = new ArrayList<>();

     public void setProductList(List<ProductStatisticResponse> productList) {
          this.productList = productList;
          notifyDataSetChanged();
     }

     @NonNull
     @Override
     public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
          View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_statistic_product, parent, false);
          return new ProductViewHolder(view);
     }

     @Override
     public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
          ProductStatisticResponse product = productList.get(position);
          holder.bind(product);
     }

     @Override
     public int getItemCount() {
          return productList != null ? productList.size() : 0;
     }

     static class ProductViewHolder extends RecyclerView.ViewHolder {
          private final TextView tvProductName;
          private final TextView tvCatalogName;
          private final TextView tvSoldQuantity;
          private final TextView tvRevenue;

          public ProductViewHolder(@NonNull View itemView) {
               super(itemView);
               tvProductName = itemView.findViewById(R.id.tvProductName);
               tvCatalogName = itemView.findViewById(R.id.tvCatalogName);
               tvSoldQuantity = itemView.findViewById(R.id.tvSoldQuantity);
               tvRevenue = itemView.findViewById(R.id.tvRevenue);
          }

          public void bind(ProductStatisticResponse product) {
               tvProductName.setText(product.getProductName());
               tvCatalogName.setText(product.getCatalogName());
               tvSoldQuantity.setText("Đã bán: " + product.getSoldQuantity());

               NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
               if (product.getRevenue() != null) {
                    tvRevenue.setText(format.format(product.getRevenue()));
               } else {
                    tvRevenue.setText("0 đ");
               }
          }
     }
}
