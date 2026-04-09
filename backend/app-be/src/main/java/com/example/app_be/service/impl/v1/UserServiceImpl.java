package com.example.app_be.service.impl.v1;

import com.example.app_be.controller.dto.request.CreateUserRequest;
import com.example.app_be.controller.dto.request.UpdateUserRequest;
import com.example.app_be.controller.dto.response.ApiResponse;
import com.example.app_be.controller.dto.response.UserResponseDto;
import com.example.app_be.core.exception.DuplicateUserException;
import com.example.app_be.core.exception.EncodeException;
import com.example.app_be.core.exception.InvalidRoleException;
import com.example.app_be.core.exception.ResourceNotFoundException;
import com.example.app_be.model.User;
import com.example.app_be.model.UserRole;
import com.example.app_be.repository.UserRepository;
import com.example.app_be.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.MethodNotAllowedException;

import java.util.List;
import java.util.UUID;

@Service
//@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String defaultPassword;

    public UserServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.security.default_password:12345678}")
            String defaultPassword) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.defaultPassword = defaultPassword;
    }

    @Override
    @PreAuthorize("hasRole('MANAGER')")
    public List<UserResponseDto> searchUsers(
            User currentUser, String query
    ) {
        List<User> users;
        if (query == null || query.isBlank())
            users = userRepository.findAllByEmailNotIgnoreCase(currentUser.getEmail());
        else users = userRepository.searchUserExcludeCurrentUser(currentUser.getEmail(), query);
        return users.stream().map(UserResponseDto::from).toList();
    }

    @Override
    public UserResponseDto getUserById(UUID id) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("User not found!")
        );
        return UserResponseDto.from(user);
    }

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

        String encodedPassword = passwordEncoder.encode(defaultPassword);
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

    @Override
    @PreAuthorize("""
            hasRole('MANAGER')
                    or 
                    (isAuthenticated() and authentication.principal.id == #id)
            """)
    public UserResponseDto updateUser(UUID id, UpdateUserRequest request) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("User not found!")
        );

        user.setFullName(request.fullName());
        user.setEmail(request.email());

        user = userRepository.save(user);
        return UserResponseDto.from(user);
    }

    @Override
    @PreAuthorize("""
                    hasRole('MANAGER')
                    or 
                    (isAuthenticated() and authentication.principal.id == #id)
            """)
    public void deleteUser(UUID id) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("User not found!")
        );

        if (user.getRole() == UserRole.MANAGER)
            throw new AuthorizationDeniedException("Cannot delete manager!");

        userRepository.delete(user);
    }
}
