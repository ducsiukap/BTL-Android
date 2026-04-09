package com.example.ddht.ui.common.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ddht.R;
import com.example.ddht.data.model.Product;
import com.example.ddht.ui.auth.LoginActivity;
import com.example.ddht.ui.common.adapter.ProductAdapter;
import com.example.ddht.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class ProductsFragment extends Fragment {
    private SessionManager sessionManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_products, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = new SessionManager(requireContext());

        RecyclerView rvProducts = view.findViewById(R.id.rvProducts);
        Button btnOpenCart = view.findViewById(R.id.btnOpenCart);
        Button btnCreateOrder = view.findViewById(R.id.btnCreateOrder);
        Button btnLogin = view.findViewById(R.id.btnProductsLogin);

        rvProducts.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvProducts.setAdapter(new ProductAdapter(fakeProducts()));

        boolean isLoggedIn = sessionManager.isLoggedIn();
        if (isLoggedIn) {
            btnLogin.setVisibility(View.GONE);
        } else {
            btnLogin.setVisibility(View.VISIBLE);
        }

        btnOpenCart.setOnClickListener(v -> Toast.makeText(requireContext(), R.string.todo_open_cart, Toast.LENGTH_SHORT).show());
        btnCreateOrder.setOnClickListener(v -> Toast.makeText(requireContext(), R.string.todo_create_order, Toast.LENGTH_SHORT).show());
        btnLogin.setOnClickListener(v -> startActivity(new Intent(requireContext(), LoginActivity.class)));
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
