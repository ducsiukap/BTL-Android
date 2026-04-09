package com.example.app_be.service;

import com.example.app_be.controller.dto.request.LoginRequest;
import com.example.app_be.controller.dto.response.LoginResponse;

public interface AuthService {

    public LoginResponse login(LoginRequest request);

}
