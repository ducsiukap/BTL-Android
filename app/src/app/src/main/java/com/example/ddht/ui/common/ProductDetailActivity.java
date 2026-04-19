package com.example.ddht.ui.common;

import android.graphics.Paint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.ddht.R;
import com.example.ddht.data.remote.dto.ApiResponse;
import com.example.ddht.data.remote.dto.ProductDto;
import com.example.ddht.data.remote.dto.ProductImageDto;
import com.example.ddht.data.repository.ProductRepository;
import com.example.ddht.ui.manager.adapter.ProductImagePagerAdapter;
import com.example.ddht.utils.SessionManager;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductDetailActivity extends AppCompatActivity {
    public static final String EXTRA_PRODUCT_ID = "extra_product_id";

    private ProductRepository productRepository;
    private SessionManager sessionManager;

    private ProgressBar progressBar;
    private TextView tvError;
    private ViewPager2 vpImages;
    private TextView tvImageIndicator;
    private TextView tvName;
    private TextView tvDescription;
    private TextView tvOriginalPrice;
    private TextView tvDiscountedPrice;
    private TextView tvSaleBadge;
    private ProductImagePagerAdapter imagePagerAdapter;

    private Long productId;
    private ProductDto currentProduct;
    private int selectedQuantity = 1;

    private TextView tvQty;
    private Button btnAddToCart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        productRepository = new ProductRepository();
        sessionManager = new SessionManager(this);

        progressBar = findViewById(R.id.productDetailProgress);
        tvError = findViewById(R.id.tvProductDetailError);
        vpImages = findViewById(R.id.vpProductDetailImages);
        tvImageIndicator = findViewById(R.id.tvProductImagePagerIndicator);
        tvName = findViewById(R.id.tvProductDetailName);
        tvDescription = findViewById(R.id.tvProductDetailDescription);
        tvOriginalPrice = findViewById(R.id.tvProductDetailOriginalPrice);
        tvDiscountedPrice = findViewById(R.id.tvProductDetailDiscountedPrice);
        tvSaleBadge = findViewById(R.id.tvProductDetailSaleBadge);

        tvQty = findViewById(R.id.tvProductDetailQty);
        ImageButton btnQtyMinus = findViewById(R.id.btnProductDetailQtyMinus);
        ImageButton btnQtyPlus = findViewById(R.id.btnProductDetailQtyPlus);
        btnAddToCart = findViewById(R.id.btnProductDetailAddToCart);

        ImageButton btnBack = findViewById(R.id.btnProductDetailBack);
        btnBack.setOnClickListener(v -> finish());

        imagePagerAdapter = new ProductImagePagerAdapter();
        vpImages.setAdapter(imagePagerAdapter);
        vpImages.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                int total = imagePagerAdapter.getItemCount();
                if (total <= 1) {
                    tvImageIndicator.setText("");
                } else {
                    tvImageIndicator.setText(getString(R.string.manager_product_image_pager_indicator, position + 1, total));
                }
            }
        });

        productId = getIntent().getLongExtra(EXTRA_PRODUCT_ID, -1);
        if (productId == -1) {
            Toast.makeText(this, R.string.manager_product_invalid_id, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Quantity logic
        btnQtyMinus.setOnClickListener(v -> {
            if (selectedQuantity > 1) {
                selectedQuantity--;
                tvQty.setText(String.valueOf(selectedQuantity));
            }
        });
        btnQtyPlus.setOnClickListener(v -> {
            selectedQuantity++;
            tvQty.setText(String.valueOf(selectedQuantity));
        });
        btnAddToCart.setOnClickListener(v -> addToCart());

        loadProduct();
    }

    private void addToCart() {
        if (currentProduct == null) return;

        double originalPrice = currentProduct.getOriginalPrice() == null ? 0 : currentProduct.getOriginalPrice();
        double displayPrice = currentProduct.getDiscountedPrice() == null ? originalPrice : currentProduct.getDiscountedPrice();
        String imageUrl = (currentProduct.getImages() != null && !currentProduct.getImages().isEmpty())
                ? currentProduct.getImages().get(0).getUrl() : null;

        com.example.ddht.data.model.Product modelProduct = new com.example.ddht.data.model.Product(
                currentProduct.getId(),
                currentProduct.getName(),
                currentProduct.getDescription(),
                displayPrice,
                originalPrice,
                Boolean.TRUE.equals(currentProduct.getSaleOff()),
                imageUrl
        );

        com.example.ddht.data.manager.CartManager.getInstance().addProduct(modelProduct, selectedQuantity);
        Toast.makeText(this, "Đã thêm " + selectedQuantity + " " + modelProduct.getName() + " vào giỏ hàng", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void loadProduct() {
        setLoading(true);
        tvError.setVisibility(View.GONE);
        productRepository.getProductById(productId).enqueue(new Callback<ApiResponse<ProductDto>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<ProductDto>> call, @NonNull Response<ApiResponse<ProductDto>> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    currentProduct = response.body().getData();
                    bindProduct(currentProduct);
                } else {
                    showError(getString(R.string.manager_product_detail_load_failed));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<ProductDto>> call, @NonNull Throwable t) {
                setLoading(false);
                showError(getString(R.string.network_error, t.getMessage()));
            }
        });
    }

    private void bindProduct(ProductDto product) {
        String name = TextUtils.isEmpty(product.getName()) ? getString(R.string.manager_product_default_name) : product.getName();
        String description = TextUtils.isEmpty(product.getDescription()) ? getString(R.string.manager_product_no_description) : product.getDescription();

        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        double originalPrice = product.getOriginalPrice() == null ? 0 : product.getOriginalPrice();
        double discountedPrice = product.getDiscountedPrice() == null ? originalPrice : product.getDiscountedPrice();

        tvName.setText(name);
        tvDescription.setText(description);

        boolean hasSale = (discountedPrice < originalPrice) || Boolean.TRUE.equals(product.getSaleOff());

        if (hasSale && discountedPrice < originalPrice) {
            tvOriginalPrice.setVisibility(View.VISIBLE);
            tvOriginalPrice.setText(formatter.format(originalPrice));
            tvOriginalPrice.setPaintFlags(tvOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            
            tvDiscountedPrice.setText(formatter.format(discountedPrice));
            tvDiscountedPrice.setTextColor(getColor(R.color.brand_primary));
            tvSaleBadge.setVisibility(View.VISIBLE);
        } else {
            tvOriginalPrice.setVisibility(View.GONE);
            tvDiscountedPrice.setText(formatter.format(originalPrice));
            tvDiscountedPrice.setTextColor(getColor(R.color.brand_text_primary));
            tvSaleBadge.setVisibility(View.GONE);
        }

        List<String> urls = new java.util.ArrayList<>();
        if (product.getImages() != null) {
            for (ProductImageDto image : product.getImages()) {
                if (image != null && !TextUtils.isEmpty(image.getUrl())) urls.add(image.getUrl());
            }
        }
        if (urls.isEmpty()) urls.add(null);
        
        imagePagerAdapter.submit(urls);
        if (urls.size() <= 1) {
            tvImageIndicator.setText("");
        } else {
            tvImageIndicator.setText(getString(R.string.manager_product_image_pager_indicator, 1, urls.size()));
        }
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }
}
