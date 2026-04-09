package com.example.ddht.ui.common;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ddht.R;
import com.example.ddht.data.model.UserRole;
import com.example.ddht.data.remote.dto.ApiResponse;
import com.example.ddht.data.remote.dto.UserDto;
import com.example.ddht.data.repository.AuthRepository;
import com.example.ddht.ui.manager.ManagerActivity;
import com.example.ddht.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SplashActivity extends AppCompatActivity {
    private SessionManager sessionManager;
    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        sessionManager = new SessionManager(this);
        authRepository = new AuthRepository();

        if (!sessionManager.isLoggedIn()) {
            openHome(false, null);
            return;
        }

        String token = sessionManager.getAccessToken();
        authRepository.getMe(token).enqueue(new Callback<ApiResponse<UserDto>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<UserDto>> call, @NonNull Response<ApiResponse<UserDto>> response) {
                if (!response.isSuccessful() || response.body() == null || response.body().getData() == null) {
                    sessionManager.clearSession();
                    Toast.makeText(SplashActivity.this, R.string.session_expired, Toast.LENGTH_SHORT).show();
                    openHome(false, null);
                    return;
                }

                UserDto me = response.body().getData();
                sessionManager.saveSession(token, me.getRole(), me.getFullName());
                UserRole role = UserRole.fromString(me.getRole());
                if (role == UserRole.MANAGER) {
                    startActivity(new Intent(SplashActivity.this, ManagerActivity.class));
                } else {
                    openHome(true, me.getFullName());
                }
                finish();
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<UserDto>> call, @NonNull Throwable throwable) {
                sessionManager.clearSession();
                openHome(false, null);
            }
        });
    }

    private void openHome(boolean loggedIn, String fullName) {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.putExtra(HomeActivity.EXTRA_IS_LOGGED_IN, loggedIn);
        if (fullName != null) {
            intent.putExtra(HomeActivity.EXTRA_USER_NAME, fullName);
        }
        startActivity(intent);
        finish();
    }
}
