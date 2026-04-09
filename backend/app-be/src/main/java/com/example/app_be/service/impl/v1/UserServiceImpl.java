package com.example.app_be.service.impl.v1;

import com.example.app_be.controller.dto.request.CreateUserRequest;
import com.example.app_be.controller.dto.response.ApiResponse;
import com.example.app_be.controller.dto.response.UserResponseDto;
import com.example.app_be.core.exception.DuplicateUserException;
import com.example.app_be.core.exception.EncodeException;
import com.example.app_be.core.exception.InvalidRoleException;
import com.example.app_be.model.User;
import com.example.app_be.model.UserRole;
import com.example.app_be.repository.UserRepository;
import com.example.app_be.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @PreAuthorize("hasRole('MANAGER')")
    public UserResponseDto createUser(
            CreateUserRequest request
    ) {

        if (userRepository.existsByEmail(request.email()))
            throw new DuplicateUserException(
                    String.format("User with email: %s already exists", request.email())
            );

        UserRole role;
        try {
            role = UserRole.valueOf(request.role().toUpperCase());
        } catch (Exception e) {
            throw new InvalidRoleException(String.format("Invalid role: %s", request.role()));
        }

        String encodedPassword = passwordEncoder.encode(request.password());
        if (encodedPassword == null)
            throw new EncodeException();

        User user = User.builder()
                .fullName(request.fullName())
                .email(request.email())
                .password(encodedPassword)
                .role(role)
                .build();

        user = userRepository.save(user);
        return UserResponseDto.from(user);
    }
}
