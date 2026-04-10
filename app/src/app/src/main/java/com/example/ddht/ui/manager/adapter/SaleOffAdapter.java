package com.example.ddht.ui.manager.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ddht.R;
import com.example.ddht.data.remote.dto.SaleOffDto;

import java.text.NumberFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SaleOffAdapter extends RecyclerView.Adapter<SaleOffAdapter.SaleOffViewHolder> {
    public interface SaleOffActionListener {
        void onOpenDetail(SaleOffDto saleOff);

        void onDelete(SaleOffDto saleOff);

        void onToggleActive(SaleOffDto saleOff, boolean isActive);
    }

    private final List<SaleOffDto> saleOffs = new ArrayList<>();
    private final SaleOffActionListener listener;

    public SaleOffAdapter(SaleOffActionListener listener) {
        this.listener = listener;
    }

    public void submit(List<SaleOffDto> items) {
        saleOffs.clear();
        if (items != null) {
            saleOffs.addAll(items);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SaleOffViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_manager_saleoff, parent, false);
        return new SaleOffViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SaleOffViewHolder holder, int position) {
        SaleOffDto item = saleOffs.get(position);
        holder.tvProductName.setText(item.getProductName());
        holder.tvDiscount.setText(holder.itemView.getContext().getString(
            R.string.manager_saleoff_discount_money,
            formatCurrency(item.getDiscount())
        ));
        holder.tvTime.setText(holder.itemView.getContext().getString(
            R.string.manager_saleoff_time_range,
            formatDateTime(item.getStartDate()),
            formatDateTime(item.getEndDate())
        ));

        boolean active = Boolean.TRUE.equals(item.getActive());
        holder.tvStatus.setText(active
            ? holder.itemView.getContext().getString(R.string.manager_saleoff_status_active)
            : holder.itemView.getContext().getString(R.string.manager_saleoff_status_inactive));
        holder.tvStatus.setTextColor(holder.itemView.getContext().getColor(active ? R.color.brand_primary : R.color.brand_error));

        holder.switchActive.setOnCheckedChangeListener(null);
        holder.switchActive.setChecked(active);
        holder.switchActive.setOnCheckedChangeListener((buttonView, isChecked) -> listener.onToggleActive(item, isChecked));

        holder.btnDelete.setOnClickListener(v -> listener.onDelete(item));
        holder.itemView.setOnClickListener(v -> listener.onOpenDetail(item));
    }

    @Override
    public int getItemCount() {
        return saleOffs.size();
    }

    private String formatCurrency(Double value) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        return formatter.format(value == null ? 0 : value);
    }

    private String formatDateTime(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return "__";
        }
        String value = raw.trim();
        DateTimeFormatter output = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.getDefault());
        try {
            Instant instant = Instant.parse(value);
            return instant.atZone(ZoneId.systemDefault()).format(output);
        } catch (Exception ignored) {
        }
        try {
            OffsetDateTime odt = OffsetDateTime.parse(value);
            return odt.atZoneSameInstant(ZoneId.systemDefault()).format(output);
        } catch (Exception ignored) {
        }
        try {
            LocalDateTime ldt = LocalDateTime.parse(value);
            return ldt.format(output);
        } catch (Exception ignored) {
        }
        return value;
    }

    static class SaleOffViewHolder extends RecyclerView.ViewHolder {
        TextView tvProductName;
        TextView tvDiscount;
        TextView tvTime;
        TextView tvStatus;
        SwitchCompat switchActive;
        ImageButton btnDelete;

        SaleOffViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tvSaleOffProductName);
            tvDiscount = itemView.findViewById(R.id.tvSaleOffDiscount);
            tvTime = itemView.findViewById(R.id.tvSaleOffTime);
            tvStatus = itemView.findViewById(R.id.tvSaleOffStatus);
            switchActive = itemView.findViewById(R.id.switchSaleOffActive);
            btnDelete = itemView.findViewById(R.id.btnDeleteSaleOff);
        }
    }
}
