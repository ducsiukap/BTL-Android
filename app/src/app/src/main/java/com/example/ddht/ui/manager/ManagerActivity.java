package com.example.ddht.ui.manager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ddht.R;
import com.example.ddht.ui.common.fragment.AccountFragment;
import com.example.ddht.ui.manager.fragment.CatalogFragment;
import com.example.ddht.ui.manager.fragment.ManagerUsersFragment;
import com.example.ddht.ui.manager.fragment.SaleOffFragment;
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
        FrameLayout layoutManagerCatalogTab = findViewById(R.id.layoutManagerCatalogTab);
        FrameLayout layoutManagerProductTab = findViewById(R.id.layoutManagerProductTab);
        FrameLayout layoutManagerSaleOffTab = findViewById(R.id.layoutManagerSaleOffTab);
        com.google.android.material.button.MaterialButtonToggleGroup toggleManagerProductTabs = findViewById(R.id.toggleManagerProductTabs);
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

        getSupportFragmentManager().beginTransaction()
            .replace(R.id.layoutManagerCatalogTab, new CatalogFragment())
            .commit();

        getSupportFragmentManager().beginTransaction()
            .replace(R.id.layoutManagerSaleOffTab, new SaleOffFragment())
            .commit();

        toggleManagerProductTabs.check(R.id.btnCatalogTab);
        toggleManagerProductTabs.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) {
                return;
            }
            layoutManagerCatalogTab.setVisibility(checkedId == R.id.btnCatalogTab ? View.VISIBLE : View.GONE);
            layoutManagerProductTab.setVisibility(checkedId == R.id.btnProductTab ? View.VISIBLE : View.GONE);
            layoutManagerSaleOffTab.setVisibility(checkedId == R.id.btnSaleOffTab ? View.VISIBLE : View.GONE);
        });
        layoutManagerCatalogTab.setVisibility(View.VISIBLE);
        layoutManagerProductTab.setVisibility(View.GONE);
        layoutManagerSaleOffTab.setVisibility(View.GONE);

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
