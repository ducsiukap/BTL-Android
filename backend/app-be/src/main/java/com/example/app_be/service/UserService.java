package com.example.app_be.service;

import com.example.app_be.controller.dto.request.CreateUserRequest;
import com.example.app_be.controller.dto.response.UserResponseDto;

public interface UserService {
    public UserResponseDto createUser(CreateUserRequest request);
}
