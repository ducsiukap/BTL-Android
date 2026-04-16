package com.example.ddht.ui.common;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ddht.R;
import com.example.ddht.data.remote.dto.ApiResponse;
import com.example.ddht.data.remote.dto.OrderItemResponse;
import com.example.ddht.data.remote.dto.OrderResponse;
import com.example.ddht.data.remote.dto.OrderStatus;
import com.example.ddht.data.repository.OrderRepository;
import com.example.ddht.ui.common.KeyboardUtils;

import java.text.NumberFormat;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderLookupActivity extends AppCompatActivity {
    private EditText edtCode;
    private ProgressBar progressBar;
    private View scrollResult;
    private TextView tvResCode, tvResStatus, tvResItems, tvResTotal;
    private OrderRepository orderRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_lookup);

        orderRepository = new OrderRepository();

        ImageButton btnBack = findViewById(R.id.btnLookupBack);
        edtCode = findViewById(R.id.edtLookupCode);
        Button btnSearch = findViewById(R.id.btnLookupSearch);
        progressBar = findViewById(R.id.lookupProgress);
        scrollResult = findViewById(R.id.scrollLookupResult);
        tvResCode = findViewById(R.id.tvLookupResultCode);
        tvResStatus = findViewById(R.id.tvLookupResultStatus);
        tvResItems = findViewById(R.id.tvLookupResultItems);
        tvResTotal = findViewById(R.id.tvLookupResultTotal);

        btnBack.setOnClickListener(v -> finish());
        btnSearch.setOnClickListener(v -> performLookup());
    }

    private void performLookup() {
        String code = edtCode.getText() == null ? "" : edtCode.getText().toString().trim().toUpperCase();
        if (TextUtils.isEmpty(code)) {
            Toast.makeText(this, "Vui lòng nhập mã đơn hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        KeyboardUtils.hideKeyboard(this);
        progressBar.setVisibility(View.VISIBLE);
        scrollResult.setVisibility(View.GONE);

        orderRepository.getOrderByCode(code).enqueue(new Callback<ApiResponse<OrderResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<OrderResponse>> call, @NonNull Response<ApiResponse<OrderResponse>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    bindOrder(response.body().getData());
                } else {
                    Toast.makeText(OrderLookupActivity.this, R.string.order_not_found, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<OrderResponse>> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(OrderLookupActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindOrder(OrderResponse order) {
        tvResCode.setText("Mã đơn: " + order.getCode());
        tvResStatus.setText(mapStatusToVietnamese(order.getStatus()));
        
        // Build items string
        StringBuilder sb = new StringBuilder();
        if (order.getItems() != null) {
            for (OrderItemResponse item : order.getItems()) {
                sb.append("• ").append(item.getProductName())
                  .append(" (x").append(item.getQuantity()).append(")\n");
            }
        }
        tvResItems.setText(sb.toString().trim());

        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        tvResTotal.setText(formatter.format(order.getTotalPrice()));

        scrollResult.setVisibility(View.VISIBLE);
    }

    private String mapStatusToVietnamese(OrderStatus status) {
        if (status == null) return "Không rõ";
        switch (status) {
            case PENDING: return "Đang chờ xử lý";
            case PREPARING: return "Đang chuẩn bị món";
            case READY: return "Sẵn sàng - Mời nhận đồ";
            case COMPLETED: return "Đã hoàn thành";
            case CANCELLED: return "Đã hủy đơn";
            default: return status.name();
        }
    }
}
