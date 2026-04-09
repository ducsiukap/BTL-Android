package com.example.app_be.service;

import com.example.app_be.controller.dto.request.ChangePasswordRequest;
import com.example.app_be.controller.dto.request.LoginRequest;
import com.example.app_be.controller.dto.response.LoginResponse;
import com.example.app_be.model.User;

import java.util.UUID;

public interface AuthService {

    public LoginResponse login(LoginRequest request);

    public void resetPassword(UUID id);

    public void changePassword(User currentUser, ChangePasswordRequest request);

}
