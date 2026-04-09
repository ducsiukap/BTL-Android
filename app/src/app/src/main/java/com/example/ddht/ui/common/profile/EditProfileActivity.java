package com.example.ddht.ui.common.profile;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
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
import com.example.ddht.data.remote.dto.UserDto;
import com.example.ddht.data.repository.AuthRepository;
import com.example.ddht.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends AppCompatActivity {
    private SessionManager sessionManager;
    private AuthRepository authRepository;

    private EditText edtFullName;
    private EditText edtEmail;
    private TextView tvError;
    private ProgressBar progressBar;
    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        sessionManager = new SessionManager(this);
        authRepository = new AuthRepository();

        edtFullName = findViewById(R.id.edtProfileFullName);
        edtEmail = findViewById(R.id.edtProfileEmail);
        tvError = findViewById(R.id.tvEditProfileError);
        progressBar = findViewById(R.id.editProfileProgressBar);
        btnSave = findViewById(R.id.btnUpdateProfile);

        findViewById(R.id.btnEditProfileBack).setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> updateProfile());

        loadCurrentProfile();
    }

    private void loadCurrentProfile() {
        String token = sessionManager.getAccessToken();
        if (TextUtils.isEmpty(token)) {
            return;
        }

        setLoading(true);
        authRepository.getMe(token).enqueue(new Callback<ApiResponse<UserDto>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<UserDto>> call,
                                   @NonNull Response<ApiResponse<UserDto>> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    UserDto user = response.body().getData();
                    edtFullName.setText(user.getFullName());
                    edtEmail.setText(user.getEmail());
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

    private void updateProfile() {
        String fullName = edtFullName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();

        if (TextUtils.isEmpty(fullName)) {
            showError(getString(R.string.error_required_field, getString(R.string.profile_fullname)));
            return;
        }
        if (TextUtils.isEmpty(email)) {
            showError(getString(R.string.error_required_field, getString(R.string.profile_email)));
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError(getString(R.string.error_invalid_email));
            return;
        }

        String token = sessionManager.getAccessToken();
        String userId = sessionManager.getUserId();
        if (TextUtils.isEmpty(token) || TextUtils.isEmpty(userId)) {
            showError(getString(R.string.session_expired));
            return;
        }

        setLoading(true);
        clearError();
        authRepository.updateUser(userId, fullName, email, token).enqueue(new Callback<ApiResponse<UserDto>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<UserDto>> call,
                                   @NonNull Response<ApiResponse<UserDto>> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    UserDto user = response.body().getData();
                    sessionManager.saveName(user.getFullName());
                    Toast.makeText(EditProfileActivity.this, R.string.profile_update_success, Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    showError(getString(R.string.profile_update_failed));
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
        btnSave.setEnabled(!isLoading);
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
