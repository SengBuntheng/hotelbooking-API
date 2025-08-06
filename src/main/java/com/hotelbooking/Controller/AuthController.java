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
@RequestMapping("/v1/auth")
@Slf4j
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserService userService;
    private final AuthService authService;
    private final EmailOtpService emailOtpService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@Valid @RequestBody UserRequest userRequest) {
        try {
            // 1. Create the user (inactive by default)
            userService.Create(userRequest);

            // 2. Send OTP to the user's email
            emailOtpService.sendOtp(userRequest.getEmail());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse(true, "Registration successful. Please check your email for the verification code."));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage()));
        } catch (Exception e) {
            log.error("Registration failed for {}: {}", userRequest.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Registration failed. Please try again."));
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse> verify(@Valid @RequestBody OtpVerificationRequest request) {
        try {
            VerificationResult result = emailOtpService.verifyOtp(request.getEmail(), request.getOtp());

            if (result.isValid()) {
                // If OTP is valid, activate the user's account
                userService.activateUser(request.getEmail());
                return ResponseEntity.ok(new ApiResponse(true, "Account activated successfully. You can now log in."));
            } else {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "OTP verification failed: " + result.getStatus()));
            }
        } catch (Exception e) {
            log.error("Verification failed for {}: {}", request.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "An unexpected error occurred during verification."));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@Valid @RequestBody AuthenticationRequest authenticationRequest) {
        try {
            LoginResponse loginResponse = authService.login(authenticationRequest);
            return ResponseEntity.ok(AuthenticationResponse.builder()
                    .token(loginResponse.getToken())
                    .success(true)
                    .user(loginResponse.getUser())
                    .message("Login successful")
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AuthenticationResponse.builder().success(false).message(e.getMessage()).build());
        }
    }
}