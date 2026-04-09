package com.example.ddht.ui.common.profile;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ddht.R;
import com.example.ddht.data.remote.dto.ApiResponse;
import com.example.ddht.data.repository.AuthRepository;
import com.example.ddht.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangePasswordActivity extends AppCompatActivity {
    private AuthRepository authRepository;
    private SessionManager sessionManager;

    private EditText edtOldPassword;
    private EditText edtNewPassword;
    private EditText edtConfirmPassword;
    private TextView tvError;
    private ProgressBar progressBar;
    private Button btnChangePassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        authRepository = new AuthRepository();
        sessionManager = new SessionManager(this);

        edtOldPassword = findViewById(R.id.edtOldPassword);
        edtNewPassword = findViewById(R.id.edtNewPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        tvError = findViewById(R.id.tvChangePasswordError);
        progressBar = findViewById(R.id.changePasswordProgressBar);
        btnChangePassword = findViewById(R.id.btnDoChangePassword);

        findViewById(R.id.btnChangePasswordBack).setOnClickListener(v -> finish());
        btnChangePassword.setOnClickListener(v -> changePassword());
    }

    private void changePassword() {
        String oldPassword = edtOldPassword.getText().toString().trim();
        String newPassword = edtNewPassword.getText().toString().trim();
        String confirmPassword = edtConfirmPassword.getText().toString().trim();

        if (!validate(oldPassword, newPassword, confirmPassword)) {
            return;
        }

        setLoading(true);
        clearError();

        String token = sessionManager.getAccessToken();
        if (token == null || token.trim().isEmpty()) {
            setLoading(false);
            showError(getString(R.string.session_expired));
            return;
        }

        authRepository.changePassword(oldPassword, newPassword, token).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Void>> call, @NonNull Response<ApiResponse<Void>> response) {
                setLoading(false);
                if (response.isSuccessful()) {
                    Toast.makeText(ChangePasswordActivity.this, R.string.password_change_success, Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    showError(getString(R.string.password_change_failed));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable throwable) {
                setLoading(false);
                showError(getString(R.string.network_error, throwable.getMessage()));
            }
        });
    }

    private boolean validate(String oldPassword, String newPassword, String confirmPassword) {
        clearError();
        if (TextUtils.isEmpty(oldPassword)) {
            showError(getString(R.string.error_required_field, getString(R.string.profile_old_password)));
            return false;
        }
        if (TextUtils.isEmpty(newPassword)) {
            showError(getString(R.string.error_required_field, getString(R.string.profile_new_password)));
            return false;
        }
        if (newPassword.length() < 6) {
            showError(getString(R.string.error_password_min_length));
            return false;
        }
        if (TextUtils.isEmpty(confirmPassword)) {
            showError(getString(R.string.error_required_field, getString(R.string.profile_confirm_password)));
            return false;
        }
        if (!newPassword.equals(confirmPassword)) {
            showError(getString(R.string.error_password_mismatch));
            return false;
        }
        return true;
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnChangePassword.setEnabled(!isLoading);
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }

    private void clearError() {
        tvError.setText("");
        tvError.setVisibility(View.GONE);
    }
}
