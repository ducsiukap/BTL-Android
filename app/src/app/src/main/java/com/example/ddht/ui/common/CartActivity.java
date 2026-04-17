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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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

        btnPlaceOrder.setOnClickListener(v -> placeOrder());

        updateUi();
    }

    private void updateUi() {
        boolean isEmpty = CartManager.getInstance().getCartItems().isEmpty();
        tvEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        layoutBottom.setVisibility(isEmpty ? View.GONE : View.VISIBLE);

        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        tvTotalPrice.setText(formatter.format(CartManager.getInstance().getTotalPrice()));
    }

    private void placeOrder() {
        List<CartItem> cartItems = CartManager.getInstance().getCartItems();
        if (cartItems.isEmpty()) {
            Toast.makeText(this, "Giỏ hàng trống", Toast.LENGTH_SHORT).show();
            return;
        }

        String token = sessionManager.getAccessToken();
        // Nếu API yêu cầu Token, chúng ta nên gửi kèm. 
        // Tôi sẽ cập nhật OrderRepository để nhận thêm token nếu cần.
        // Hiện tại, tôi giả định API cần Bearer Token.
        String bearerToken = token != null ? "Bearer " + token : null;

        List<OrderItemRequest> itemRequests = new ArrayList<>();
        for (CartItem item : cartItems) {
            itemRequests.add(new OrderItemRequest(item.getProduct().getId(), item.getQuantity()));
        }

        CreateOrderRequest request = new CreateOrderRequest(itemRequests);

        progressBar.setVisibility(View.VISIBLE);
        // Lưu ý: Cần cập nhật OrderRepository.createOrder để nhận token nếu Backend yêu cầu
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
        String paymentUrl = order.getPaymentUrl();

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Đặt hàng thành công!")
                .setMessage(getString(R.string.order_created_success, code))
                .setCancelable(false);

        if (paymentUrl != null && !paymentUrl.isEmpty()) {
            // Tạo ImageView để chứa mã QR
            ImageView qrImageView = new ImageView(this);
            int padding = (int) (16 * getResources().getDisplayMetrics().density);
            qrImageView.setPadding(padding, padding, padding, padding);
            
            Bitmap qrBitmap = generateQrCode(paymentUrl);
            if (qrBitmap != null) {
                qrImageView.setImageBitmap(qrBitmap);
                builder.setView(qrImageView);
                builder.setMessage(getString(R.string.order_created_success, code) + "\n\nBạn có thể quét mã QR dưới đây để thanh toán ngay:");
            }
        }

        builder.setNeutralButton(R.string.order_copy_code, (dialog, which) -> {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("Order Code", code);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(CartActivity.this, "Đã sao chép mã đơn hàng", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setPositiveButton(R.string.common_close, (dialog, which) -> finish())
                .show();
    }

    private Bitmap generateQrCode(String data) {
        QRCodeWriter writer = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, 512, 512);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            return bmp;
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void onCartChanged() {
        updateUi();
    }
}
