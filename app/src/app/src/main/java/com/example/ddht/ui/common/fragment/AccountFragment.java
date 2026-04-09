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
import androidx.fragment.app.FragmentTransaction;

import com.example.ddht.R;
import com.example.ddht.ui.auth.LoginActivity;
import com.example.ddht.utils.SessionManager;

public class AccountFragment extends Fragment {
    private SessionManager sessionManager;
    private LinearLayout guestContainer;
    private FrameLayout profileContainer;

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
        Button btnLogin = view.findViewById(R.id.btnAccountLogin);

        boolean isLoggedIn = sessionManager.isLoggedIn();

        if (isLoggedIn) {
            guestContainer.setVisibility(View.GONE);
            profileContainer.setVisibility(View.VISIBLE);
            loadProfileFragment();
        } else {
            guestContainer.setVisibility(View.VISIBLE);
            profileContainer.setVisibility(View.GONE);
            btnLogin.setOnClickListener(v -> startActivity(new Intent(requireContext(), LoginActivity.class)));
        }
    }

    private void loadProfileFragment() {
        ProfileFragment profileFragment = new ProfileFragment();
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.profileContainer, profileFragment);
        transaction.commit();
    }
}
