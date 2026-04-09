package com.example.ddht.ui.common.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ddht.R;
import com.example.ddht.data.remote.dto.ApiResponse;
import com.example.ddht.data.remote.dto.UserDto;
import com.example.ddht.data.repository.AuthRepository;
import com.example.ddht.ui.common.SplashActivity;
import com.example.ddht.ui.common.profile.ChangePasswordActivity;
import com.example.ddht.ui.common.profile.EditProfileActivity;
import com.example.ddht.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {
    private SessionManager sessionManager;
    private AuthRepository authRepository;

    private TextView tvDisplayFullName;
    private TextView tvDisplayEmail;
    private Button btnEditInfo;
    private Button btnChangePassword;
    private Button btnLogout;
    private ProgressBar progressBar;
    private TextView tvErrorMessage;

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

        tvDisplayFullName = view.findViewById(R.id.tvDisplayProfileFullName);
        tvDisplayEmail = view.findViewById(R.id.tvDisplayProfileEmail);
        btnEditInfo = view.findViewById(R.id.btnOpenEditProfile);
        btnChangePassword = view.findViewById(R.id.btnChangePassword);
        btnLogout = view.findViewById(R.id.btnProfileLogout);
        progressBar = view.findViewById(R.id.profileProgressBar);
        tvErrorMessage = view.findViewById(R.id.tvProfileError);

        btnEditInfo.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), EditProfileActivity.class))
        );
        btnChangePassword.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), ChangePasswordActivity.class))
        );
        btnLogout.setOnClickListener(v -> {
            sessionManager.clearSession();
            Intent intent = new Intent(requireContext(), SplashActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
        });

        loadProfileInfo();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadProfileInfo();
    }

    private void loadProfileInfo() {
        String token = sessionManager.getAccessToken();
        if (token == null || token.trim().isEmpty()) {
            tvDisplayFullName.setText("N/A");
            tvDisplayEmail.setText("N/A");
            return;
        }

        setLoading(true);
        clearError();

        authRepository.getMe(token).enqueue(new Callback<ApiResponse<UserDto>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<UserDto>> call,
                                   @NonNull Response<ApiResponse<UserDto>> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    UserDto user = response.body().getData();
                    tvDisplayFullName.setText(user.getFullName() != null ? user.getFullName() : "N/A");
                    tvDisplayEmail.setText(user.getEmail() != null ? user.getEmail() : "N/A");
                    if (user.getFullName() != null) {
                        sessionManager.saveName(user.getFullName());
                    }
                } else {
                    showError(getString(R.string.profile_load_failed));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<UserDto>> call, @NonNull Throwable throwable) {
                setLoading(false);
                showError(getString(R.string.network_error, throwable.getMessage()));
            }
        });
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnEditInfo.setEnabled(!isLoading);
        btnChangePassword.setEnabled(!isLoading);
        btnLogout.setEnabled(!isLoading);
    }

    private void showError(String message) {
        tvErrorMessage.setText(message);
        tvErrorMessage.setVisibility(View.VISIBLE);
    }

    private void clearError() {
        tvErrorMessage.setVisibility(View.GONE);
        tvErrorMessage.setText("");
    }
}
