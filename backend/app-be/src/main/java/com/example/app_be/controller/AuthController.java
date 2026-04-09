package com.example.app_be.controller;

import com.example.app_be.controller.dto.request.LoginRequest;
import com.example.app_be.controller.dto.response.ApiResponse;
import com.example.app_be.controller.dto.response.LoginResponse;
import com.example.app_be.core.annotation.ApiV1;
import com.example.app_be.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

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
}
