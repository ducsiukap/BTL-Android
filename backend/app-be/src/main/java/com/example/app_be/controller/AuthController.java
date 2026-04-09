package com.example.app_be.controller;

import com.example.app_be.controller.dto.request.ChangePasswordRequest;
import com.example.app_be.controller.dto.request.LoginRequest;
import com.example.app_be.controller.dto.response.ApiResponse;
import com.example.app_be.controller.dto.response.LoginResponse;
import com.example.app_be.core.annotation.ApiV1;
import com.example.app_be.model.User;
import com.example.app_be.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.UUID;

@ApiV1
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest loginRequest
    ) {
        LoginResponse credentials = authService.login(loginRequest);
        return ResponseEntity.ok(ApiResponse.success(credentials));
    }

    @PostMapping("/reset-password/{id}")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @PathVariable UUID id
    ) {
        authService.resetPassword(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        authService.changePassword(currentUser, request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

}
