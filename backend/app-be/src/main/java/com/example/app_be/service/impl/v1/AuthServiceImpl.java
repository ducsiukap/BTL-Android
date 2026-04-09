package com.example.app_be.service.impl.v1;

import com.example.app_be.controller.dto.request.LoginRequest;
import com.example.app_be.controller.dto.response.LoginResponse;
import com.example.app_be.core.exception.InvalidCredentialsException;
import com.example.app_be.core.security.JwtService;
import com.example.app_be.model.User;
import com.example.app_be.repository.UserRepository;
import com.example.app_be.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() ->
                        new InvalidCredentialsException(
                                "Could not find user with email: \"%s\""
                                        .formatted(request.email())));

        String encodedPassword = passwordEncoder.encode(request.password());
        if (encodedPassword == null ||
                !encodedPassword.equals(user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        String accessToken = jwtService.generateToken(user);

        return new LoginResponse(
                user.getId(), user.getFullName(),
                user.getRole().name(),
                accessToken, null
        );
    }
}
