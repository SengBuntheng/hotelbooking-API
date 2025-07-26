package com.hotelbooking.service.impl;

import com.hotelbooking.Config.JwtUtils;
import com.hotelbooking.Repository.UserRepository;
import com.hotelbooking.dto.LoginRequest;
import com.hotelbooking.dto.LoginResponse;
import com.hotelbooking.model.User;
import com.hotelbooking.service.AuthService;
import com.hotelbooking.service.handler.AuthHandlerService;
import jakarta.validation.constraints.Email;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static com.hotelbooking.service.handler.AuthHandlerService.mapToUserResponse;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {


    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final AuthHandlerService authHandlerService;
    public AuthServiceImpl(JwtUtils jwtUtils, PasswordEncoder passwordEncoder, UserRepository userRepository, AuthenticationManager authenticationManager, AuthHandlerService authHandlerService) {
        this.jwtUtils = jwtUtils;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.authHandlerService = authHandlerService;
    }


    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.isActive()) {
            return new LoginResponse(false, "Account not activated. Please verify your email.", null);
        }

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash())) {
            return new LoginResponse(false, "Invalid credentials", null);
        }

        String token = jwtUtils.generateToken(user.getEmail());
        return new LoginResponse(true, "Login successful", token);
    }

    @Override
    public LoginResponse loginWithOtp(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.isActive()) {
            return new LoginResponse(false, "Account not activated. Please verify your email.", null);
        }

        String token = jwtUtils.generateToken(user.getEmail());
        return new LoginResponse(true, "Login successful", token);
    }
}