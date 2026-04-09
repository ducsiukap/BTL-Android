package com.example.ddht.ui.common.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ddht.R;
import com.example.ddht.ui.auth.LoginActivity;
import com.example.ddht.utils.SessionManager;

public class AccountFragment extends Fragment {
    private SessionManager sessionManager;
    private LinearLayout guestContainer;
    private FrameLayout profileContainer;
    private TextView tvGuestTitle;
    private TextView tvGuestDescription;
    private Button btnLogin;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_account, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = new SessionManager(requireContext());
        guestContainer = view.findViewById(R.id.guestContainer);
        profileContainer = view.findViewById(R.id.profileContainer);
        tvGuestTitle = view.findViewById(R.id.tvGuestTitle);
        tvGuestDescription = view.findViewById(R.id.tvGuestDescription);
        btnLogin = view.findViewById(R.id.btnGoToLogin);

        btnLogin.setOnClickListener(v -> startActivity(new Intent(requireContext(), LoginActivity.class)));
        renderState();
    }

    @Override
    public void onResume() {
        super.onResume();
        renderState();
    }

    private void renderState() {
        boolean loggedIn = sessionManager.isLoggedIn();
        guestContainer.setVisibility(loggedIn ? View.GONE : View.VISIBLE);
        profileContainer.setVisibility(loggedIn ? View.VISIBLE : View.GONE);

        if (loggedIn) {
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.profileContainer, new ProfileFragment())
                    .commit();
        } else {
            tvGuestTitle.setText(R.string.account_guest_title);
            tvGuestDescription.setText(R.string.account_guest_desc);
        }
    }
}
