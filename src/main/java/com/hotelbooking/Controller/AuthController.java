package com.hotelbooking.Controller;

import com.hotelbooking.Enum.VerificationResult;
import com.hotelbooking.dto.*;
import com.hotelbooking.service.AuthService;
import com.hotelbooking.service.EmailOtpService;
import com.hotelbooking.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Slf4j
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserService userService;
    private final AuthService authService;
    private final EmailOtpService emailOtpService;


    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody UserRequest userRequest) {
        try {
            userService.Create(userRequest);
            emailOtpService.sendOtp(userRequest.getEmail());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Registration successful. Please check your email for the verification code.", null));

        } catch (IllegalArgumentException e) {
            log.warn("Registration failed for {}: {}", userRequest.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Registration failed for {}: {}", userRequest.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Registration failed. Please try again."));
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<String>> verify(@Valid @RequestBody OtpVerificationRequest request) {
        try {
            VerificationResult result = emailOtpService.verifyOtp(request.getEmail(), request.getOtp());

            if (result.isValid()) {
                userService.activateUser(request.getEmail());
                return ResponseEntity.ok(ApiResponse.success("Account activated successfully. You can now log in.", null));
            } else {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("OTP verification failed: " + result.getStatus()));
            }
        } catch (Exception e) {
            log.error("Verification failed for {}: {}", request.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("An unexpected error occurred during verification."));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> login(@Valid @RequestBody AuthenticationRequest authenticationRequest) {
        try {
            LoginResponse loginResponse = authService.login(authenticationRequest);
            AuthenticationResponse response = AuthenticationResponse.builder()
                    .token(loginResponse.getToken())
                    .refreshToken(loginResponse.getRefreshToken())
                    .success(true)
                    .user(loginResponse.getUser())
                    .message("Login successful")
                    .build();

            return ResponseEntity.ok(ApiResponse.success("Login successful", response));
        } catch (Exception e) {
            log.error("Login failed for {}: {}", authenticationRequest.getEmail(), e.getMessage());
            AuthenticationResponse errorResponse = AuthenticationResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication failed"));
        }
    }

    @PostMapping("/login-otp")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> loginWithOtp(@Valid @RequestBody OtpLoginRequest request) {
        try {
            LoginResponse loginResponse = authService.loginWithOtp(request.getEmail(), request.getOtp());
            AuthenticationResponse response = AuthenticationResponse.builder()
                    .token(loginResponse.getToken())
                    .refreshToken(loginResponse.getRefreshToken())
                    .success(true)
                    .user(loginResponse.getUser())
                    .message("OTP login successful")
                    .build();

            return ResponseEntity.ok(ApiResponse.success("OTP login successful", response));
        } catch (Exception e) {
            log.error("OTP login failed for {}: {}", request.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("OTP authentication failed"));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> refreshToken(@RequestBody RefreshTokenRequest request) {
        try {
            AuthenticationResponse response = authService.refreshToken(request.getRefreshToken());
            return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", response));
        } catch (Exception e) {
            log.error("Token refresh failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Token refresh failed"));
        }
    }

    @PostMapping("/send-otp")
    public ResponseEntity<ApiResponse<String>> sendOtp(@Valid @RequestBody OtpLoginRequest.SendOtpRequest request) {
        try {
            emailOtpService.sendOtp(request.getEmail());
            return ResponseEntity.ok(ApiResponse.success("OTP sent successfully", null));
        } catch (Exception e) {
            log.error("Failed to send OTP to {}: {}", request.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to send OTP"));
        }
    }
}