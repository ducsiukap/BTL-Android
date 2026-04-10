package com.example.ddht.ui.common.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ddht.R;
import com.example.ddht.data.remote.dto.ApiResponse;
import com.example.ddht.data.remote.dto.UserDto;
import com.example.ddht.data.repository.AuthRepository;
import com.example.ddht.ui.auth.LoginActivity;
import com.example.ddht.ui.common.profile.ChangePasswordActivity;
import com.example.ddht.ui.common.profile.EditProfileActivity;
import com.example.ddht.ui.common.SplashActivity;
import com.example.ddht.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {
    private SessionManager sessionManager;
    private AuthRepository authRepository;

    private TextView tvFullName;
    private TextView tvEmail;
    private TextView tvError;
    private ProgressBar progressBar;
    private Button btnEditInfo;
    private Button btnChangePassword;
    private ImageButton btnLogout;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = new SessionManager(requireContext());
        authRepository = new AuthRepository();

        tvFullName = view.findViewById(R.id.tvDisplayProfileFullName);
        tvEmail = view.findViewById(R.id.tvDisplayProfileEmail);
        tvError = view.findViewById(R.id.tvProfileError);
        progressBar = view.findViewById(R.id.profileProgressBar);
        btnEditInfo = view.findViewById(R.id.btnOpenEditProfile);
        btnChangePassword = view.findViewById(R.id.btnOpenChangePassword);
        btnLogout = view.findViewById(R.id.btnProfileLogout);

        btnEditInfo.setOnClickListener(v -> startActivity(new Intent(requireContext(), EditProfileActivity.class)));
        btnChangePassword.setOnClickListener(v -> startActivity(new Intent(requireContext(), ChangePasswordActivity.class)));
        btnLogout.setOnClickListener(v -> logout());

        loadProfile();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadProfile();
    }

    private void loadProfile() {
        String token = sessionManager.getAccessToken();
        if (token == null || token.trim().isEmpty()) {
            tvFullName.setText("N/A");
            tvEmail.setText("N/A");
            return;
        }

        setLoading(true);
        clearError();
        authRepository.getMe(token).enqueue(new Callback<ApiResponse<UserDto>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<UserDto>> call,
                                   @NonNull Response<ApiResponse<UserDto>> response) {
                if (!isAdded()) {
                    return;
                }
                setLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    UserDto user = response.body().getData();
                    tvFullName.setText(user.getFullName() == null ? "N/A" : user.getFullName());
                    tvEmail.setText(user.getEmail() == null ? "N/A" : user.getEmail());
                    if (user.getFullName() != null) {
                        sessionManager.saveName(user.getFullName());
                    }
                } else if (response.code() == 401) {
                    handleExpiredSession();
                } else {
                    showError(getString(R.string.profile_load_failed));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<UserDto>> call, @NonNull Throwable throwable) {
                if (!isAdded()) {
                    return;
                }
                setLoading(false);
                showError(getString(R.string.network_error, throwable.getMessage()));
            }
        });
    }

    private void logout() {
        sessionManager.clearSession();
        Intent intent = new Intent(requireContext(), SplashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnEditInfo.setEnabled(!isLoading);
        btnChangePassword.setEnabled(!isLoading);
        btnLogout.setEnabled(!isLoading);
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }

    private void handleExpiredSession() {
        if (!isAdded()) {
            return;
        }
        sessionManager.clearSession();
        Toast.makeText(requireContext(), R.string.session_expired, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    private void clearError() {
        tvError.setText("");
        tvError.setVisibility(View.GONE);
    }
}
