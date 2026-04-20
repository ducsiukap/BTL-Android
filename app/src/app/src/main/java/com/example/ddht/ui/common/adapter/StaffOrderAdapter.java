package com.example.ddht.ui.common.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ddht.R;
import com.example.ddht.data.remote.dto.OrderItemResponse;
import com.example.ddht.data.remote.dto.OrderResponse;
import com.example.ddht.data.remote.dto.OrderStatus;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StaffOrderAdapter extends RecyclerView.Adapter<StaffOrderAdapter.StaffOrderViewHolder> {
    private final List<OrderResponse> orders = new ArrayList<>();
    private final OnOrderActionListener listener;

    public interface OnOrderActionListener {
        void onUpdateStatus(OrderResponse order, OrderStatus nextStatus);
        void onMarkAsPaid(OrderResponse order);
        void onCancelOrder(OrderResponse order);
        void onItemClick(OrderResponse order);
    }

    public StaffOrderAdapter(OnOrderActionListener listener) {
        this.listener = listener;
    }

    public void submitList(List<OrderResponse> newList) {
        orders.clear();
        if (newList != null) {
            orders.addAll(newList);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StaffOrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_staff_order, parent, false);
        return new StaffOrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StaffOrderViewHolder holder, int position) {
        OrderResponse order = orders.get(position);
        holder.tvCode.setText(order.getCode());
        holder.tvStatus.setText(mapStatus(order.getStatus()));
        
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        holder.tvTotal.setText(formatter.format(order.getTotalPrice()));

        StringBuilder sb = new StringBuilder();
        if (order.getItems() != null) {
            for (OrderItemResponse item : order.getItems()) {
                sb.append("• ").append(item.getProductName()).append(" (x").append(item.getQuantity()).append(")\n");
            }
        }
        holder.tvItems.setText(sb.toString().trim());

        if (order.getStatus() == OrderStatus.PENDING) {
            holder.btnAction.setVisibility(View.VISIBLE);
            holder.btnAction.setText("XÁC NHẬN THANH TOÁN");
            holder.btnAction.setOnClickListener(v -> {
                if (listener != null) listener.onMarkAsPaid(order);
            });
            holder.btnCancel.setVisibility(View.VISIBLE);
            holder.btnCancel.setOnClickListener(v -> {
                if (listener != null) listener.onCancelOrder(order);
            });
        } else if (order.getStatus() == OrderStatus.PREPARING) {
            holder.btnAction.setVisibility(View.VISIBLE);
            holder.btnAction.setText("HOÀN THÀNH");
            holder.btnAction.setOnClickListener(v -> {
                if (listener != null) listener.onUpdateStatus(order, OrderStatus.COMPLETED);
            });
            holder.btnCancel.setVisibility(View.VISIBLE);
            holder.btnCancel.setOnClickListener(v -> {
                if (listener != null) listener.onCancelOrder(order);
            });
        } else {
            holder.btnAction.setVisibility(View.GONE);
            holder.btnCancel.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(order);
        });
    }

    private String mapStatus(OrderStatus status) {
        if (status == null) return "N/A";
        switch (status) {
            case PENDING: return "CHỜ THANH TOÁN";
            case PREPARING: return "ĐANG CHẾ BIẾN";
            case COMPLETED: return "HOÀN THÀNH";
            case CANCELLED: return "ĐÃ HỦY";
            default: return status.name();
        }
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    static class StaffOrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvCode, tvStatus, tvTime, tvItems, tvTotal;
        Button btnAction, btnCancel;

        public StaffOrderViewHolder(@NonNull View view) {
            super(view);
            tvCode = view.findViewById(R.id.tvStaffOrderCode);
            tvStatus = view.findViewById(R.id.tvStaffOrderStatus);
            tvTime = view.findViewById(R.id.tvStaffOrderTime);
            tvItems = view.findViewById(R.id.tvStaffOrderItems);
            tvTotal = view.findViewById(R.id.tvStaffOrderTotal);
            btnAction = view.findViewById(R.id.btnStaffOrderAction);
            btnCancel = view.findViewById(R.id.btnStaffOrderCancel);
        }
    }
}
