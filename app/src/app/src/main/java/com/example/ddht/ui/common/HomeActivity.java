package com.example.ddht.ui.common;

import android.os.Bundle;
import android.text.TextUtils;
import android.graphics.Rect;
import android.util.TypedValue;
import android.view.inputmethod.EditorInfo;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ddht.R;
import com.example.ddht.data.model.Product;
import com.example.ddht.data.remote.dto.ApiResponse;
import com.example.ddht.data.remote.dto.CatalogDto;
import com.example.ddht.data.remote.dto.ProductDto;
import com.example.ddht.data.remote.dto.ProductImageDto;
import com.example.ddht.data.repository.CatalogRepository;
import com.example.ddht.data.repository.ProductRepository;
import com.example.ddht.ui.common.adapter.ProductAdapter;
import com.example.ddht.ui.common.fragment.AccountFragment;
import com.example.ddht.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {
    private SessionManager sessionManager;
    private CatalogRepository catalogRepository;
    private ProductRepository productRepository;
    private ProductAdapter productAdapter;
    private EditText edtProductSearch;
    private TextView tvProductsError;
    private LinearLayout layoutCatalogFilters;
    private final List<CatalogDto> catalogsCache = new ArrayList<>();
    private Long selectedCatalogId = null;
    private String currentQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        sessionManager = new SessionManager(this);
        catalogRepository = new CatalogRepository();
        productRepository = new ProductRepository();

        RecyclerView rvProducts = findViewById(R.id.rvProducts);
        edtProductSearch = findViewById(R.id.edtProductSearch);
        ImageButton btnSearchProducts = findViewById(R.id.btnSearchProducts);
        Button btnOpenCart = findViewById(R.id.btnOpenCart);
        Button btnCreateOrder = findViewById(R.id.btnCreateOrder);
        TextView tvWelcome = findViewById(R.id.tvWelcome);
        tvProductsError = findViewById(R.id.tvProductsError);
        layoutCatalogFilters = findViewById(R.id.layoutCatalogFilters);
        LinearLayout layoutProducts = findViewById(R.id.layoutHomeProducts);
        android.widget.FrameLayout layoutAccount = findViewById(R.id.layoutHomeAccount);
        LinearLayout layoutOrders = findViewById(R.id.layoutHomeOrders);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);

        rvProducts.setLayoutManager(new GridLayoutManager(this, 2));
        int spacingPx = dpToPx(8);
        if (rvProducts.getItemDecorationCount() == 0) {
            rvProducts.addItemDecoration(new GridSpacingItemDecoration(2, spacingPx));
        }
        productAdapter = new ProductAdapter(new ArrayList<>());
        rvProducts.setAdapter(productAdapter);

        btnSearchProducts.setOnClickListener(v -> searchProducts());
        edtProductSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                searchProducts();
                return true;
            }
            return false;
        });

        if (sessionManager.isLoggedIn()) {
            String userName = sessionManager.getUserName();
            tvWelcome.setText(getString(R.string.home_staff_welcome, userName == null ? "Nhân viên" : userName));
        } else {
            tvWelcome.setText(R.string.home_guest_welcome);
        }

        String role = sessionManager.getUserRole();
        boolean isStaff = role != null && role.equalsIgnoreCase("STAFF");
        if (bottomNavigationView.getMenu().findItem(R.id.nav_orders) != null) {
            bottomNavigationView.getMenu().findItem(R.id.nav_orders).setVisible(isStaff);
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.layoutHomeAccount, new AccountFragment())
                .commit();

        bottomNavigationView.setSelectedItemId(R.id.nav_products);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_products) {
                layoutProducts.setVisibility(View.VISIBLE);
                layoutOrders.setVisibility(View.GONE);
                layoutAccount.setVisibility(View.GONE);
                return true;
            }
            if (item.getItemId() == R.id.nav_orders && isStaff) {
                layoutProducts.setVisibility(View.GONE);
                layoutOrders.setVisibility(View.VISIBLE);
                layoutAccount.setVisibility(View.GONE);
                return true;
            }
            if (item.getItemId() == R.id.nav_account) {
                layoutProducts.setVisibility(View.GONE);
                layoutOrders.setVisibility(View.GONE);
                layoutAccount.setVisibility(View.VISIBLE);
                return true;
            }
            return false;
        });

        btnOpenCart.setOnClickListener(v -> Toast.makeText(this, R.string.todo_open_cart, Toast.LENGTH_SHORT).show());
        btnCreateOrder.setOnClickListener(v -> Toast.makeText(this, R.string.todo_create_order, Toast.LENGTH_SHORT).show());

        loadCatalogs();
        loadProducts(currentQuery);
    }

    private void searchProducts() {
        currentQuery = edtProductSearch.getText() == null ? "" : edtProductSearch.getText().toString().trim();
        loadProducts(currentQuery);
    }

    private void loadProducts(String query) {
        clearProductsError();
        productRepository.searchProducts(query, selectedCatalogId, 0, 50).enqueue(new Callback<ApiResponse<List<ProductDto>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<ProductDto>>> call,
                                   @NonNull Response<ApiResponse<List<ProductDto>>> response) {
                if (!response.isSuccessful() || response.body() == null || response.body().getData() == null) {
                    showProductsError(getString(R.string.home_products_load_failed));
                    productAdapter.submit(new ArrayList<>());
                    return;
                }

                List<Product> mappedProducts = mapProducts(response.body().getData());
                productAdapter.submit(mappedProducts);
                if (mappedProducts.isEmpty()) {
                    showProductsError(getString(R.string.home_products_empty));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<ProductDto>>> call, @NonNull Throwable throwable) {
                showProductsError(getString(R.string.network_error, throwable.getMessage()));
                productAdapter.submit(new ArrayList<>());
            }
        });
    }

    private List<Product> mapProducts(List<ProductDto> dtoList) {
        List<Product> mapped = new ArrayList<>();
        for (ProductDto dto : dtoList) {
            if (dto == null) {
                continue;
            }
            String name = TextUtils.isEmpty(dto.getName()) ? "Sản phẩm" : dto.getName();
            String subtitle = TextUtils.isEmpty(dto.getDescription()) ? "" : dto.getDescription();
            double originalPrice = dto.getOriginalPrice() == null ? 0 : dto.getOriginalPrice();
            double displayPrice = resolveDisplayPrice(dto);
            boolean saleOff = Boolean.TRUE.equals(dto.getSaleOff())
                    && dto.getDiscountedPrice() != null
                    && dto.getDiscountedPrice() < originalPrice;
            String imageUrl = resolveImageUrl(dto);
            mapped.add(new Product(name, subtitle, displayPrice, originalPrice, saleOff, imageUrl));
        }
        return mapped;
    }

    private double resolveDisplayPrice(ProductDto dto) {
        if (Boolean.TRUE.equals(dto.getSaleOff()) && dto.getDiscountedPrice() != null) {
            return dto.getDiscountedPrice();
        }
        if (dto.getOriginalPrice() != null) {
            return dto.getOriginalPrice();
        }
        return 0;
    }

    private String resolveImageUrl(ProductDto dto) {
        if (dto.getImages() == null || dto.getImages().isEmpty()) {
            return null;
        }
        ProductImageDto first = dto.getImages().get(0);
        if (first == null || TextUtils.isEmpty(first.getUrl())) {
            return null;
        }
        return first.getUrl();
    }

    private void showProductsError(String message) {
        tvProductsError.setText(message);
        tvProductsError.setVisibility(View.VISIBLE);
    }

    private void clearProductsError() {
        tvProductsError.setText("");
        tvProductsError.setVisibility(View.GONE);
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    private void loadCatalogs() {
        renderCatalogChips();
        catalogRepository.getAllCatalogs().enqueue(new Callback<ApiResponse<List<CatalogDto>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<CatalogDto>>> call,
                                   @NonNull Response<ApiResponse<List<CatalogDto>>> response) {
                if (!response.isSuccessful() || response.body() == null || response.body().getData() == null) {
                    return;
                }
                catalogsCache.clear();
                catalogsCache.addAll(response.body().getData());
                renderCatalogChips();
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<CatalogDto>>> call, @NonNull Throwable throwable) {
                // Keep default filter only when catalog API is unavailable.
            }
        });
    }

    private void renderCatalogChips() {
        layoutCatalogFilters.removeAllViews();
        addCatalogChip(getString(R.string.home_catalog_all), null, selectedCatalogId == null);
        for (CatalogDto catalog : catalogsCache) {
            if (catalog == null || catalog.getId() == null || TextUtils.isEmpty(catalog.getName())) {
                continue;
            }
            addCatalogChip(catalog.getName(), catalog.getId(), catalog.getId().equals(selectedCatalogId));
        }
    }

    private void addCatalogChip(String label, Long catalogId, boolean selected) {
        TextView chip = new TextView(this);
        chip.setText(label);
        chip.setSelected(selected);
        chip.setBackgroundResource(R.drawable.bg_catalog_chip);
        chip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        chip.setTextColor(getColor(selected ? android.R.color.white : R.color.brand_primary));
        chip.setPadding(dpToPx(12), dpToPx(8), dpToPx(12), dpToPx(8));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMarginEnd(dpToPx(8));
        chip.setLayoutParams(params);

        chip.setOnClickListener(v -> {
            selectedCatalogId = catalogId;
            renderCatalogChips();
            loadProducts(currentQuery);
        });

        layoutCatalogFilters.addView(chip);
    }

    private static class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {
        private final int spanCount;
        private final int spacing;

        GridSpacingItemDecoration(int spanCount, int spacing) {
            this.spanCount = spanCount;
            this.spacing = spacing;
        }

        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view);
            if (position == RecyclerView.NO_POSITION) {
                return;
            }

            int column = position % spanCount;
            outRect.left = spacing - column * spacing / spanCount;
            outRect.right = (column + 1) * spacing / spanCount;
            if (position >= spanCount) {
                outRect.top = spacing;
            }
            outRect.bottom = spacing;
        }
    }
}
