package com.example.ddht.ui.manager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ddht.R;
import com.example.ddht.ui.common.SplashActivity;
import com.example.ddht.ui.common.fragment.AccountFragment;
import com.example.ddht.ui.manager.fragment.ManagerUsersFragment;
import com.example.ddht.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ManagerActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager);

        SessionManager sessionManager = new SessionManager(this);
        LinearLayout layoutProducts = findViewById(R.id.layoutManagerProducts);
        FrameLayout layoutAccount = findViewById(R.id.layoutManagerAccount);
        FrameLayout layoutUsers = findViewById(R.id.layoutManagerUsers);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationManager);
        TextView tvGreeting = findViewById(R.id.tvManagerGreeting);

        String fullName = sessionManager.getUserName();
        if (fullName == null || fullName.trim().isEmpty()) {
            fullName = "Quản lý";
        }
        tvGreeting.setText(getString(R.string.manager_greeting, fullName));

        // Load account fragment initially
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.layoutManagerAccount, new AccountFragment())
                .commit();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.layoutManagerUsers, new ManagerUsersFragment())
                .commit();

        bottomNavigationView.setSelectedItemId(R.id.nav_products);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_products) {
                layoutProducts.setVisibility(View.VISIBLE);
                layoutAccount.setVisibility(View.GONE);
                layoutUsers.setVisibility(View.GONE);
                return true;
            }
            if (item.getItemId() == R.id.nav_account) {
                layoutProducts.setVisibility(View.GONE);
                layoutAccount.setVisibility(View.VISIBLE);
                layoutUsers.setVisibility(View.GONE);
                return true;
            }
            if (item.getItemId() == R.id.nav_users) {
                layoutProducts.setVisibility(View.GONE);
                layoutAccount.setVisibility(View.GONE);
                layoutUsers.setVisibility(View.VISIBLE);
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload account fragment to catch login state changes
        if (getSupportFragmentManager().findFragmentById(R.id.layoutManagerAccount) != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.layoutManagerAccount, new AccountFragment())
                    .commit();
        }
        if (getSupportFragmentManager().findFragmentById(R.id.layoutManagerUsers) != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.layoutManagerUsers, new ManagerUsersFragment())
                    .commit();
        }
    }
}
