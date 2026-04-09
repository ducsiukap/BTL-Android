package com.example.ddht.ui.manager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ddht.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.ddht.ui.common.SplashActivity;
import com.example.ddht.utils.SessionManager;

public class ManagerActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager);

        SessionManager sessionManager = new SessionManager(this);
        LinearLayout layoutManagerProducts = findViewById(R.id.layoutManagerProducts);
        LinearLayout layoutManagerAccount = findViewById(R.id.layoutManagerAccount);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationManager);
        TextView tvGreeting = findViewById(R.id.tvManagerGreeting);
        Button btnManagerManageAccount = findViewById(R.id.btnManagerManageAccount);
        Button btnLogout = findViewById(R.id.btnManagerLogout);

        String fullName = sessionManager.getUserName();
        if (fullName == null || fullName.trim().isEmpty()) {
            fullName = "Quản lý";
        }
        tvGreeting.setText(getString(R.string.manager_greeting, fullName));

        bottomNavigationView.setSelectedItemId(R.id.nav_products);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_products) {
                layoutManagerProducts.setVisibility(View.VISIBLE);
                layoutManagerAccount.setVisibility(View.GONE);
                return true;
            }
            if (item.getItemId() == R.id.nav_account) {
                layoutManagerProducts.setVisibility(View.GONE);
                layoutManagerAccount.setVisibility(View.VISIBLE);
                return true;
            }
            return false;
        });

        btnManagerManageAccount.setOnClickListener(v ->
                Toast.makeText(this, R.string.account_fake_message, Toast.LENGTH_SHORT).show()
        );

        btnLogout.setOnClickListener(v -> {
            sessionManager.clearSession();
            Intent intent = new Intent(this, SplashActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
}
