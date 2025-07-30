package com.hotelbooking.Controller;

import com.hotelbooking.Enum.VerificationResult;
import com.hotelbooking.dto.*;
import com.hotelbooking.service.AuthService;
import com.hotelbooking.service.EmailOtpService;
import com.hotelbooking.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/auth")
@Slf4j
public class AuthController {

    private final UserService userService;
    private final AuthService authService;
    private final EmailOtpService emailOtpService;

    public AuthController(UserService userService, AuthService authService, EmailOtpService emailOtpService) {
        this.userService = userService;
        this.authService = authService;
        this.emailOtpService = emailOtpService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@Valid @RequestBody UserRequest userRequest) {
        try {
            log.info("Registration attempt for user: {}", userRequest.getEmail());
            userService.Create(userRequest);
            emailOtpService.sendOtp(userRequest.getEmail());
            ApiResponse response = new ApiResponse(true, "User registered successfully. Please check your email for an OTP to activate your account.");
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Registration failed for {}: {}", userRequest.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse> verifyRegistration(@Valid @RequestBody OtpVerificationRequest request) {
        try {
            VerificationResult result = emailOtpService.verifyOtp(request.getEmail(), request.getOtp());
            if (result.isValid()) {
                userService.activateUser(request.getEmail());
                return ResponseEntity.ok(new ApiResponse(true, "Account activated successfully. You can now log in."));
            }
            return ResponseEntity.badRequest().body(new ApiResponse(false, "OTP verification failed: " + result.getStatus()));
        } catch (Exception e) {
            log.error("Verification failed for {}: {}", request.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "An unexpected error occurred during verification."));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            log.info("Login attempt for user: {}", loginRequest.getEmail());
            LoginResponse response = authService.login(loginRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Login failed for {}: {}", loginRequest.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @PostMapping("/request-otp")
    public ResponseEntity<ApiResponse> requestLoginOtp(@RequestParam String email) {
        try {
            if (!userService.userExists(email)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse(false, "An account with this email does not exist."));
            }
            emailOtpService.sendOtp(email);
            return ResponseEntity.ok(new ApiResponse(true, "An OTP has been sent to your email."));
        } catch (Exception e) {
            log.error("Request OTP failed for {}: {}", email, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @PostMapping("/login-with-otp")
    public ResponseEntity<?> loginWithOtp(@Valid @RequestBody OtpLoginRequest request) {
        try {
            log.info("OTP login attempt for user: {}", request.getEmail());
            LoginResponse response = authService.loginWithOtp(request.getEmail(), request.getOtp());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("OTP login failed for {}: {}", request.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }
}
