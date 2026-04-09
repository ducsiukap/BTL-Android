package com.example.app_be.controller;

import com.example.app_be.controller.dto.request.CreateUserRequest;
import com.example.app_be.controller.dto.response.ApiResponse;
import com.example.app_be.controller.dto.response.UserResponseDto;
import com.example.app_be.core.annotation.ApiV1;
import com.example.app_be.model.User;
import com.example.app_be.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@ApiV1
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    ResponseEntity<ApiResponse<UserResponseDto>> createUser(
            @Valid @RequestBody CreateUserRequest request
    ) {

        UserResponseDto user = userService.createUser(request);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @GetMapping
    ResponseEntity<ApiResponse<List<UserResponseDto>>> searchUsers(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false, defaultValue = "") String query
    ) {

        List<UserResponseDto> users = userService.searchUsers(currentUser, query);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @GetMapping("/me")
    ResponseEntity<ApiResponse<UserResponseDto>> getMe(
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                userService.getUserById(currentUser.getId())
        ));
    }

    @GetMapping("/{id}")
    ResponseEntity<ApiResponse<UserResponseDto>> getUserById(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                userService.getUserById(id)
        ));
    }

    @PostMapping("/{id}")
    ResponseEntity<ApiResponse<UserResponseDto>> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody CreateUserRequest request
    ) {
        return null;
    }

}
