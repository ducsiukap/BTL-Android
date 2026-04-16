package com.example.ddht.ui.common;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ddht.R;
import com.example.ddht.data.manager.CartManager;
import com.example.ddht.data.model.CartItem;
import com.example.ddht.data.remote.dto.ApiResponse;
import com.example.ddht.data.remote.dto.CreateOrderRequest;
import com.example.ddht.data.remote.dto.OrderItemRequest;
import com.example.ddht.data.remote.dto.OrderResponse;
import com.example.ddht.data.repository.OrderRepository;
import com.example.ddht.ui.common.adapter.CartAdapter;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartActivity extends AppCompatActivity implements CartAdapter.OnCartChangeListener {
    private CartAdapter adapter;
    private TextView tvTotalPrice;
    private View layoutBottom;
    private TextView tvEmpty;
    private ProgressBar progressBar;
    private OrderRepository orderRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        orderRepository = new OrderRepository();

        ImageButton btnBack = findViewById(R.id.btnCartBack);
        RecyclerView rvItems = findViewById(R.id.rvCartItems);
        tvTotalPrice = findViewById(R.id.tvCartTotalPrice);
        layoutBottom = findViewById(R.id.layoutCartBottom);
        tvEmpty = findViewById(R.id.tvCartEmpty);
        progressBar = findViewById(R.id.cartProgress);
        Button btnPlaceOrder = findViewById(R.id.btnCartPlaceOrder);

        btnBack.setOnClickListener(v -> finish());

        rvItems.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CartAdapter(this);
        rvItems.setAdapter(adapter);

        btnPlaceOrder.setOnClickListener(v -> placeOrder());

        updateUi();
    }

    private void updateUi() {
        boolean isEmpty = CartManager.getInstance().getItemCount() == 0;
        tvEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        layoutBottom.setVisibility(isEmpty ? View.GONE : View.VISIBLE);

        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        tvTotalPrice.setText(formatter.format(CartManager.getInstance().getTotalPrice()));
    }


    public void onCartChangeListener() {
        updateUi();
    }

    private void placeOrder() {
        List<CartItem> cartItems = CartManager.getInstance().getCartItems();
        if (cartItems.isEmpty()) return;

        List<OrderItemRequest> itemRequests = new ArrayList<>();
        for (CartItem item : cartItems) {
            itemRequests.add(new OrderItemRequest(item.getProduct().getId(), item.getQuantity()));
        }

        CreateOrderRequest request = new CreateOrderRequest(itemRequests);

        progressBar.setVisibility(View.VISIBLE);
        orderRepository.createOrder(request).enqueue(new Callback<ApiResponse<OrderResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<OrderResponse>> call, @NonNull Response<ApiResponse<OrderResponse>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    String orderCode = response.body().getData().getCode();
                    CartManager.getInstance().clearCart();
                    showSuccessDialog(orderCode);
                } else {
                    Toast.makeText(CartActivity.this, "Đặt hàng thất bại. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<OrderResponse>> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(CartActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showSuccessDialog(String code) {
        new AlertDialog.Builder(this)
                .setTitle("Đặt hàng thành công!")
                .setMessage(getString(R.string.order_created_success, code))
                .setCancelable(false)
                .setNeutralButton(R.string.order_copy_code, (dialog, which) -> {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("Order Code", code);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(CartActivity.this, "Đã sao chép mã đơn hàng", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setPositiveButton(R.string.common_close, (dialog, which) -> finish())
                .show();
    }

    @Override
    public void onCartChanged() {
        updateUi();
    }
}
