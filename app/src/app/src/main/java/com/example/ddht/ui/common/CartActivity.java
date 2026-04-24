package com.example.ddht.ui.common;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ddht.data.remote.SimpleStompClient;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

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
import com.example.ddht.utils.SessionManager;

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
    private SessionManager sessionManager;
    private AlertDialog successDialog;
    private SimpleStompClient stompClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        orderRepository = new OrderRepository();
        sessionManager = new SessionManager(this);

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

        btnPlaceOrder.setOnClickListener(v -> showConfirmOrderDialog());

        updateUi();
    }

    private void updateUi() {
        boolean isEmpty = CartManager.getInstance().getCartItems().isEmpty();
        tvEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        layoutBottom.setVisibility(isEmpty ? View.GONE : View.VISIBLE);

        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        tvTotalPrice.setText(formatter.format(CartManager.getInstance().getTotalPrice()));
    }

    private void showConfirmOrderDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.layout_dialog_order_detail, null);

        TextView tvTitle = dialogView.findViewById(R.id.tvDialogOrderTitle);
        TextView tvCode = dialogView.findViewById(R.id.tvDialogOrderCode);
        TextView tvStatus = dialogView.findViewById(R.id.tvDialogOrderStatus);
        TextView tvTotal = dialogView.findViewById(R.id.tvDialogOrderTotal);
        LinearLayout itemsContainer = dialogView.findViewById(R.id.layoutDialogOrderItems);
        Button btnConfirm = dialogView.findViewById(R.id.btnDialogOrderClose);

        tvTitle.setText("Xác nhận đơn hàng");
        tvCode.setText("Vui lòng kiểm tra lại các món đã chọn");
        tvStatus.setVisibility(View.GONE);

        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        double total = CartManager.getInstance().getTotalPrice();
        tvTotal.setText(formatter.format(total));

        List<CartItem> cartItems = CartManager.getInstance().getCartItems();
        for (CartItem item : cartItems) {
            View itemView = getLayoutInflater().inflate(R.layout.item_dialog_order_detail, itemsContainer, false);
            ((TextView) itemView.findViewById(R.id.tvOrderItemQty)).setText(String.valueOf(item.getQuantity()));
            ((TextView) itemView.findViewById(R.id.tvOrderItemName)).setText(item.getProduct().getName());
            ((TextView) itemView.findViewById(R.id.tvOrderItemPrice)).setText(formatter.format(item.getProduct().getDisplayPrice()));
            ((TextView) itemView.findViewById(R.id.tvOrderItemSubtotal)).setText(formatter.format(item.getProduct().getDisplayPrice() * item.getQuantity()));
            itemsContainer.addView(itemView);
        }

        btnConfirm.setText("XÁC NHẬN ĐẶT HÀNG");
        AlertDialog dialog = builder.setView(dialogView).create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        btnConfirm.setOnClickListener(v -> {
            dialog.dismiss();
            placeOrder();
        });
        dialog.show();
    }

    private void placeOrder() {
        List<CartItem> cartItems = CartManager.getInstance().getCartItems();
        if (cartItems.isEmpty()) {
            Toast.makeText(this, "Giỏ hàng trống", Toast.LENGTH_SHORT).show();
            return;
        }

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
                    OrderResponse orderResponse = response.body().getData();
                    CartManager.getInstance().clearCart();
                    showSuccessDialog(orderResponse);
                } else {
                    String errorMsg = "Đặt hàng thất bại";
                    if (response.code() == 401) errorMsg += ": Bạn cần đăng nhập";
                    Toast.makeText(CartActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<OrderResponse>> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(CartActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showSuccessDialog(OrderResponse order) {
        String code = order.getCode();

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Đặt hàng thành công!")
                .setMessage(getString(R.string.order_created_success, code) + "\n\nVui lòng đến quầy thu ngân để thanh toán đơn hàng.")
                .setCancelable(false);

        successDialog = builder.setNeutralButton(R.string.order_copy_code, (dialog, which) -> {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("Order Code", code);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(CartActivity.this, "Đã sao chép mã đơn hàng", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setPositiveButton(R.string.common_close, (dialog, which) -> finish())
                .show();
        initWebSocket(code);
    }

    private void initWebSocket(String orderCode) {
        String wsUrl = "ws://10.0.2.2:3333/ws-order";
        stompClient = new SimpleStompClient(wsUrl);
        stompClient.connect();
        stompClient.subscribe("/topic/order/" + orderCode, payload -> {
            if (successDialog != null && successDialog.isShowing()) {
                successDialog.dismiss();
                Toast.makeText(CartActivity.this, "Thanh toán thành công!", Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (stompClient != null) {
            stompClient.disconnect();
        }
    }


    @Override
    public void onCartChanged() {
        updateUi();
    }
}
