package com.hotelbooking.service.impl;

import com.hotelbooking.Config.JwtService;
import com.hotelbooking.Config.UserPrincipal;
import com.hotelbooking.GlobalException.OtpException;
import com.hotelbooking.Repository.UserRepository;
import com.hotelbooking.dto.AuthenticationRequest;
import com.hotelbooking.dto.AuthenticationResponse;
import com.hotelbooking.dto.LoginResponse;
import com.hotelbooking.dto.UserRespone;
import com.hotelbooking.model.User;
import com.hotelbooking.service.AuthService;
import com.hotelbooking.service.EmailOtpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final EmailOtpService emailOtpService;


    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(AuthenticationRequest authenticationRequest) {
        try {
            User user = userRepository.findByEmail(authenticationRequest.getEmail())
                    .orElseThrow(() -> new OtpException.AuthenticationFailedException("Check credentials"));

            if (!user.getActive()) {
                throw new OtpException.AuthenticationFailedException("Account is not active. Please verify your email.");
            }

            if (!passwordEncoder.matches(authenticationRequest.getPassword(), user.getPasswordHash())) {
                throw new OtpException.AuthenticationFailedException("Check credentials");
            }

            String accessToken = jwtService.generateToken(user.getEmail());
            String refreshToken = jwtService.generateRefreshToken(user.getEmail());
            updateLastLogin(user);

            return buildLoginResponse(user, accessToken, refreshToken);

        } catch (Exception e) {
            log.error("Authentication failed for email: {}: {}", authenticationRequest.getEmail(), e.getMessage());
            throw new OtpException.AuthenticationFailedException(e.getMessage());
        }
    }

    private void updateLastLogin(User user) {
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
    }

    private LoginResponse buildLoginResponse(User user, String accessToken, String refreshToken) {
        Objects.requireNonNull(user, "User cannot be null");
        return LoginResponse.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .user(mapToUserResponse(user))
                .build();
    }

    private UserRespone mapToUserResponse(User user) {
        return UserRespone.builder()
                .id(user.getId())
                .uuid(user.getUuid())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .createdAt(user.getCreateDate())
                .build();
    }

    @Override
    public LoginResponse loginWithOtp(String email, String otp) {
        try {
            if (!emailOtpService.verifyOtp(email, otp).isValid()) {
                throw new OtpException.OtpVerificationException("OTP verification failed");
            }

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new OtpException.UserNotFoundException("User not found with email: " + email));

            if (!user.getActive()) {
                throw new OtpException.AuthenticationFailedException("Account is inactive");
            }

            String accessToken = jwtService.generateToken(email);
            String refreshToken = jwtService.generateRefreshToken(email);
            updateLastLogin(user);

            return buildLoginResponse(user, accessToken, refreshToken);
        } catch (Exception e) {
            log.error("OTP login failed for email: {}", email, e);
            throw new OtpException.AuthenticationFailedException("OTP login failed");
        }
    }

    @Override
    public AuthenticationResponse refreshToken(String refreshToken) {
        try {
            String username = jwtService.extractUsername(refreshToken);
            UserDetails userDetails = userRepository.findByEmail(username)
                    .map(UserPrincipal::new)
                    .orElseThrow(() -> new OtpException.AuthenticationFailedException("User not found from refresh token"));

            if (jwtService.validateToken(refreshToken, userDetails)) {
                String newAccessToken = jwtService.generateToken(username);
                return AuthenticationResponse.builder()
                        .success(true)
                        .message("Token refreshed successfully")
                        .token(newAccessToken)
                        .refreshToken(refreshToken)
                        .build();
            } else {
                throw new OtpException.AuthenticationFailedException("Invalid refresh token");
            }
        } catch (Exception e) {
            log.error("Token refresh failed: {}", e.getMessage());
            throw new OtpException.AuthenticationFailedException("Invalid refresh token: " + e.getMessage());
        }
    }
}
