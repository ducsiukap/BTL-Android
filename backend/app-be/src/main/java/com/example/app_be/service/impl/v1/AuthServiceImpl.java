package com.example.app_be.service.impl.v1;

import com.example.app_be.controller.dto.request.ChangePasswordRequest;
import com.example.app_be.controller.dto.request.LoginRequest;
import com.example.app_be.controller.dto.response.LoginResponse;
import com.example.app_be.core.exception.InvalidCredentialsException;
import com.example.app_be.core.exception.ChangePasswordException;
import com.example.app_be.core.exception.ResourceNotFoundException;
import com.example.app_be.core.security.JwtService;
import com.example.app_be.model.User;
import com.example.app_be.repository.UserRepository;
import com.example.app_be.service.AuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
//@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final String defaultPassword;

    public AuthServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            @Value("${app.security.default_password:12345678}")
            String defaultPassword
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.defaultPassword = defaultPassword;
    }

    @Transactional(readOnly = true)
    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() ->
                        new InvalidCredentialsException(
                                "Could not find user with email: \"%s\""
                                        .formatted(request.email())));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        String accessToken = jwtService.generateToken(user);

        return new LoginResponse(
                user.getId(), user.getFullName(),
                user.getRole().name(),
                accessToken, null
        );
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    @PreAuthorize("hasRole('MANAGER')")
    public void resetPassword(UUID id) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("User not found!")
        );

        user.setPassword(passwordEncoder.encode(defaultPassword));
        userRepository.save(user);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void changePassword(User currentUser, ChangePasswordRequest request) {
        if (request.oldPassword().equals(request.newPassword()))
            throw new ChangePasswordException("New password cannot be the same as old password!");

        if (!passwordEncoder.matches(request.oldPassword(), currentUser.getPassword()))
            throw new ChangePasswordException("Old password does not match!");

        currentUser.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(currentUser);
    }
}
