package com.example.ddht.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ddht.R;
import com.example.ddht.data.model.UserRole;
import com.example.ddht.data.remote.dto.ApiResponse;
import com.example.ddht.data.remote.dto.LoginResponseData;
import com.example.ddht.data.repository.AuthRepository;
import com.example.ddht.ui.common.HomeActivity;
import com.example.ddht.ui.manager.ManagerActivity;
import com.example.ddht.utils.SessionManager;
import com.google.android.material.textfield.TextInputLayout;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private TextInputLayout tilEmail;
    private TextInputLayout tilPassword;
    private EditText edtEmail;
    private EditText edtPassword;
    private TextView tvLoginError;
    private ProgressBar progressBar;
    private Button btnLogin;

    private AuthRepository authRepository;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        tvLoginError = findViewById(R.id.tvLoginError);
        progressBar = findViewById(R.id.progressBar);
        btnLogin = findViewById(R.id.btnLogin);

        authRepository = new AuthRepository();
        sessionManager = new SessionManager(this);

        btnLogin.setOnClickListener(v -> attemptLogin());

        edtEmail.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                tilEmail.setError(null);
                hideGeneralError();
            }
        });

        edtPassword.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                tilPassword.setError(null);
                hideGeneralError();
            }
        });
    }

    private void attemptLogin() {
        hideGeneralError();
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (!validateInputs(email, password)) {
            return;
        }

        setLoading(true);
        authRepository.login(email, password).enqueue(new Callback<ApiResponse<LoginResponseData>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<LoginResponseData>> call,
                                   @NonNull Response<ApiResponse<LoginResponseData>> response) {
                setLoading(false);

                if (!response.isSuccessful() || response.body() == null || response.body().getData() == null) {
                    String fallback = getString(R.string.login_failed);
                    if (response.body() != null && !TextUtils.isEmpty(response.body().getMessage())) {
                        showGeneralError(response.body().getMessage());
                    } else {
                        showGeneralError(fallback);
                    }
                    return;
                }

                LoginResponseData data = response.body().getData();
                sessionManager.saveSession(data.getAccessToken(), data.getRole(), data.getFullName());

                UserRole role = UserRole.fromString(data.getRole());
                if (role == UserRole.MANAGER) {
                    startActivity(new Intent(LoginActivity.this, ManagerActivity.class));
                } else {
                    Intent staffIntent = new Intent(LoginActivity.this, HomeActivity.class);
                    staffIntent.putExtra(HomeActivity.EXTRA_IS_LOGGED_IN, true);
                    staffIntent.putExtra(HomeActivity.EXTRA_USER_NAME, data.getFullName());
                    startActivity(staffIntent);
                }
                finish();
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<LoginResponseData>> call, @NonNull Throwable throwable) {
                setLoading(false);
                showGeneralError(getString(R.string.network_error, throwable.getMessage()));
            }
        });
    }

    private boolean validateInputs(String email, String password) {
        boolean isValid = true;

        tilEmail.setError(null);
        tilPassword.setError(null);

        if (TextUtils.isEmpty(email)) {
            tilEmail.setError(getString(R.string.error_required_email));
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError(getString(R.string.error_invalid_email));
            isValid = false;
        }

        if (TextUtils.isEmpty(password)) {
            tilPassword.setError(getString(R.string.error_required_password));
            isValid = false;
        } else if (password.length() < 6) {
            tilPassword.setError(getString(R.string.error_invalid_password));
            isValid = false;
        }

        if (!isValid) {
            showGeneralError(getString(R.string.error_login_general));
        }

        return isValid;
    }

    private void showGeneralError(String message) {
        tvLoginError.setText(message);
        tvLoginError.setVisibility(View.VISIBLE);
    }

    private void hideGeneralError() {
        tvLoginError.setVisibility(View.GONE);
        tvLoginError.setText(R.string.login_error_placeholder);
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!isLoading);
    }
}
