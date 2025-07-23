package com.hotelbooking.service.impl;

import com.hotelbooking.Config.JwtUtils;
import com.hotelbooking.Repository.UserRepository;
import com.hotelbooking.dto.LoginRequest;
import com.hotelbooking.dto.LoginResponse;
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
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Generate JWT token
            String token = jwtUtils.generateToken(loginRequest.getEmail());

            // Retrieve user from DB
            var user = userRepository.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Build response
            LoginResponse response = new LoginResponse();
            response.setToken(token);
            response.setUser(mapToUserResponse(user));
            return response;

        } catch (Exception e) {
            log.error("Authentication failed for user: {}", loginRequest.getEmail(), e);
            throw new RuntimeException("Authentication failed: " + e.getMessage());
        }
    }

}


