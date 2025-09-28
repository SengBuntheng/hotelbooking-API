package com.hotelbooking.service.impl;


import com.hotelbooking.Config.JwtService;
import com.hotelbooking.Config.UserPrincipal;
import com.hotelbooking.Enum.VerificationResult;
import com.hotelbooking.Repository.UserRepository;
import com.hotelbooking.dto.AuthenticationRequest;
import com.hotelbooking.dto.AuthenticationResponse;
import com.hotelbooking.dto.LoginResponse;
import com.hotelbooking.dto.UserResponse;
import com.hotelbooking.model.User;
import com.hotelbooking.service.AuthService;
import com.hotelbooking.service.EmailOtpService;
import com.hotelbooking.service.handler.AuthHandlerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.auth.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthHandlerService authHandlerService;
    private final EmailOtpService emailOtpService;

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(AuthenticationRequest authenticationRequest) throws AuthenticationException {
        try {
            validateLoginRequest(authenticationRequest);

            User user = userRepository.findByEmail(authenticationRequest.getEmail().toLowerCase())
                    .orElseThrow(() -> new AuthenticationException("Invalid credentials"));

            if (!user.getActive()) {
                throw new AuthenticationException("Account is not active. Please verify your email.");
            }

            if (!authHandlerService.verifyPassword(authenticationRequest.getPassword(), user.getPasswordHash())) {
                throw new AuthenticationException("Invalid credentials");
            }

            String accessToken = jwtService.generateToken(user.getEmail());
            String refreshToken = jwtService.generateRefreshToken(user.getEmail());

            updateLastLogin(user);
            log.info("Successful login for user: {}", user.getEmail());

            return buildLoginResponse(user, accessToken, refreshToken);

        } catch (AuthenticationException e) {
            log.warn("Authentication failed for email: {}: {}", authenticationRequest.getEmail(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during authentication for email: {}: {}", authenticationRequest.getEmail(), e.getMessage());
            throw new AuthenticationException("Authentication failed");
        }
    }

    @Override
    public LoginResponse loginWithOtp(String email, String otp) throws AuthenticationException {
        try {
            if (email == null || email.trim().isEmpty()) {
                throw new AuthenticationException("Email is required");
            }

            if (otp == null || otp.trim().isEmpty()) {
                throw new AuthenticationException("OTP is required");
            }

            VerificationResult verificationResult = emailOtpService.verifyOtp(email.toLowerCase(), otp);
            if (!verificationResult.isValid()) {
                throw new AuthenticationException("Invalid or expired OTP");
            }

            User user = userRepository.findByEmail(email.toLowerCase())
                    .orElseThrow(() -> new AuthenticationException("User not found"));

            if (!user.getActive()) {
                throw new AuthenticationException("Account is inactive");
            }

            String accessToken = jwtService.generateToken(email);
            String refreshToken = jwtService.generateRefreshToken(email);

            updateLastLogin(user);
            log.info("Successful OTP login for user: {}", email);

            return buildLoginResponse(user, accessToken, refreshToken);

        } catch (AuthenticationException e) {
            log.warn("OTP login failed for email: {}: {}", email, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during OTP login for email: {}: {}", email, e.getMessage());
            throw new AuthenticationException("OTP login failed");
        }
    }

    @Override
    public AuthenticationResponse refreshToken(String refreshToken) throws AuthenticationException {
        try {
            if (refreshToken == null || refreshToken.trim().isEmpty()) {
                throw new AuthenticationException("Refresh token is required");
            }

            String username = jwtService.extractUsername(refreshToken);
            if (username == null) {
                throw new AuthenticationException("Invalid refresh token");
            }

            UserDetails userDetails = userRepository.findByEmail(username)
                    .map(UserPrincipal::new)
                    .orElseThrow(() -> new AuthenticationException("User not found"));

            if (!jwtService.validateToken(refreshToken, userDetails)) {
                throw new AuthenticationException("Invalid or expired refresh token");
            }

            String newAccessToken = jwtService.generateToken(username);
            log.info("Token refreshed successfully for user: {}", username);

            return AuthenticationResponse.builder()
                    .success(true)
                    .message("Token refreshed successfully")
                    .token(newAccessToken)
                    .refreshToken(refreshToken)
                    .build();

        } catch (AuthenticationException e) {
            log.warn("Token refresh failed: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during token refresh: {}", e.getMessage());
            throw new AuthenticationException("Token refresh failed");
        }
    }

    private void validateLoginRequest(AuthenticationRequest request) throws AuthenticationException {
        if (request == null) {
            throw new AuthenticationException("Login request is required");
        }

        if (!authHandlerService.isValidEmail(request.getEmail())) {
            throw new AuthenticationException("Invalid email format");
        }

        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new AuthenticationException("Password is required");
        }
    }

    private void updateLastLogin(User user) {
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
    }

    private LoginResponse buildLoginResponse(User user, String accessToken, String refreshToken) {
        Objects.requireNonNull(user, "User cannot be null");

        UserResponse userResponse = authHandlerService.mapToUserResponse(user);
        // Set token for response
        userResponse.setToken(accessToken);

        return LoginResponse.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .user(userResponse)
                .build();
    }
}