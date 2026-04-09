package com.example.ddht.ui.manager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.ddht.R;
import com.example.ddht.ui.common.fragment.AccountFragment;
import com.example.ddht.ui.manager.fragment.ManagerUsersFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.ddht.ui.common.SplashActivity;
import com.example.ddht.utils.SessionManager;

public class ManagerActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager);

        SessionManager sessionManager = new SessionManager(this);
        View layoutManagerProducts = findViewById(R.id.layoutManagerProducts);
        View layoutManagerUsers = findViewById(R.id.layoutManagerUsers);
        View layoutManagerAccount = findViewById(R.id.layoutManagerAccount);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationManager);
        TextView tvGreeting = findViewById(R.id.tvManagerGreeting);

        String fullName = sessionManager.getUserName();
        if (fullName == null || fullName.trim().isEmpty()) {
            fullName = "Quản lý";
        }
        tvGreeting.setText(getString(R.string.manager_greeting, fullName));

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.layoutManagerUsers, new ManagerUsersFragment())
                .commit();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.layoutManagerAccount, new AccountFragment())
                .commit();

        bottomNavigationView.setSelectedItemId(R.id.nav_products);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_products) {
                layoutManagerProducts.setVisibility(View.VISIBLE);
                layoutManagerUsers.setVisibility(View.GONE);
                layoutManagerAccount.setVisibility(View.GONE);
                return true;
            }
            if (item.getItemId() == R.id.nav_users) {
                layoutManagerProducts.setVisibility(View.GONE);
                layoutManagerUsers.setVisibility(View.VISIBLE);
                layoutManagerAccount.setVisibility(View.GONE);
                return true;
            }
            if (item.getItemId() == R.id.nav_account) {
                layoutManagerProducts.setVisibility(View.GONE);
                layoutManagerUsers.setVisibility(View.GONE);
                layoutManagerAccount.setVisibility(View.VISIBLE);
                return true;
            }
            return false;
        });
    }

}
