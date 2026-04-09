package com.example.ddht.ui.common;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ddht.R;
import com.example.ddht.data.model.Product;
import com.example.ddht.ui.common.adapter.ProductAdapter;
import com.example.ddht.ui.common.fragment.AccountFragment;
import com.example.ddht.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {
    private SessionManager sessionManager;

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
        FrameLayout layoutAccount = findViewById(R.id.layoutHomeAccount);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);

        rvProducts.setLayoutManager(new LinearLayoutManager(this));
        rvProducts.setAdapter(new ProductAdapter(fakeProducts()));

        boolean isLoggedIn = sessionManager.isLoggedIn();
        String userName = sessionManager.getUserName();

        if (isLoggedIn) {
            tvWelcome.setText(getString(R.string.home_staff_welcome, userName == null ? "Nhân viên" : userName));
        } else {
            tvWelcome.setText(R.string.home_guest_welcome);
        }

        // Load account fragment initially
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.layoutHomeAccount, new AccountFragment())
                .commit();

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
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload account fragment to catch login state changes
        if (getSupportFragmentManager().findFragmentById(R.id.layoutHomeAccount) != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.layoutHomeAccount, new AccountFragment())
                    .commit();
        }
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
