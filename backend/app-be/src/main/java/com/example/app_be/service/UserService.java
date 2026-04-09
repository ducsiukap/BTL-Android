package com.example.app_be.service;

import com.example.app_be.controller.dto.request.CreateUserRequest;
import com.example.app_be.controller.dto.response.UserResponseDto;
import com.example.app_be.model.User;

import java.util.List;
import java.util.UUID;

public interface UserService {
    public UserResponseDto createUser(CreateUserRequest request);

    public List<UserResponseDto> searchUsers(User currentUser, String query);

    public UserResponseDto getUserById(UUID id);
}
