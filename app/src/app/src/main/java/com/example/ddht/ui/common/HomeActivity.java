package com.example.ddht.ui.common;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ddht.R;
import com.example.ddht.data.model.Product;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.ddht.ui.auth.LoginActivity;
import com.example.ddht.ui.common.adapter.ProductAdapter;
import com.example.ddht.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {
    public static final String EXTRA_IS_LOGGED_IN = "extra_is_logged_in";
    public static final String EXTRA_USER_NAME = "extra_user_name";

    private SessionManager sessionManager;
    private boolean isLoggedIn;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        sessionManager = new SessionManager(this);

        RecyclerView rvProducts = findViewById(R.id.rvProducts);
        Button btnOpenCart = findViewById(R.id.btnOpenCart);
        Button btnCreateOrder = findViewById(R.id.btnCreateOrder);
        TextView tvWelcome = findViewById(R.id.tvWelcome);
        LinearLayout layoutProducts = findViewById(R.id.layoutHomeProducts);
        LinearLayout layoutAccount = findViewById(R.id.layoutHomeAccount);
        TextView tvAccountTitle = findViewById(R.id.tvAccountTitle);
        TextView tvAccountDescription = findViewById(R.id.tvAccountDescription);
        Button btnAccountPrimary = findViewById(R.id.btnAccountPrimary);
        Button btnAccountSecondary = findViewById(R.id.btnAccountSecondary);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);

        rvProducts.setLayoutManager(new LinearLayoutManager(this));
        rvProducts.setAdapter(new ProductAdapter(fakeProducts()));

        isLoggedIn = getIntent().getBooleanExtra(EXTRA_IS_LOGGED_IN, sessionManager.isLoggedIn());
        userName = getIntent().getStringExtra(EXTRA_USER_NAME);

        if (isLoggedIn) {
            if (userName == null || userName.trim().isEmpty()) {
                userName = sessionManager.getUserName();
            }
            tvWelcome.setText(getString(R.string.home_staff_welcome, userName == null ? "Nhân viên" : userName));
        } else {
            tvWelcome.setText(R.string.home_guest_welcome);
        }

        updateAccountContent(tvAccountTitle, tvAccountDescription, btnAccountPrimary, btnAccountSecondary);

        bottomNavigationView.setSelectedItemId(R.id.nav_products);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_products) {
                layoutProducts.setVisibility(View.VISIBLE);
                layoutAccount.setVisibility(View.GONE);
                return true;
            }
            if (item.getItemId() == R.id.nav_account) {
                layoutProducts.setVisibility(View.GONE);
                layoutAccount.setVisibility(View.VISIBLE);
                return true;
            }
            return false;
        });

        btnOpenCart.setOnClickListener(v -> Toast.makeText(this, R.string.todo_open_cart, Toast.LENGTH_SHORT).show());
        btnCreateOrder.setOnClickListener(v -> Toast.makeText(this, R.string.todo_create_order, Toast.LENGTH_SHORT).show());

        btnAccountPrimary.setOnClickListener(v -> {
            if (!isLoggedIn) {
                startActivity(new Intent(this, LoginActivity.class));
                return;
            }
            Toast.makeText(this, R.string.account_fake_message, Toast.LENGTH_SHORT).show();
        });

        btnAccountSecondary.setOnClickListener(v -> {
            sessionManager.clearSession();
            Intent splash = new Intent(this, SplashActivity.class);
            splash.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(splash);
        });
    }

    private void updateAccountContent(TextView tvAccountTitle,
                                      TextView tvAccountDescription,
                                      Button btnAccountPrimary,
                                      Button btnAccountSecondary) {
        if (!isLoggedIn) {
            tvAccountTitle.setText(R.string.account_guest_title);
            tvAccountDescription.setText(R.string.account_guest_desc);
            btnAccountPrimary.setText(R.string.login_button);
            btnAccountSecondary.setVisibility(View.GONE);
            return;
        }

        tvAccountTitle.setText(R.string.account_staff_title);
        tvAccountDescription.setText(R.string.account_staff_desc);
        btnAccountPrimary.setText(R.string.manage_account_button);
        btnAccountSecondary.setVisibility(View.VISIBLE);
    }

    private List<Product> fakeProducts() {
        List<Product> products = new ArrayList<>();
        products.add(new Product("Trà Đào Cam Sả", "Món bán chạy", 45000));
        products.add(new Product("Cà phê sữa đá", "Đậm vị truyền thống", 29000));
        products.add(new Product("Bánh mì gà nướng", "Ăn nhanh tại quán", 35000));
        products.add(new Product("Nước cam tươi", "Không đường", 32000));
        return products;
    }
}
