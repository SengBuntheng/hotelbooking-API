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
@RequestMapping("v1/account")
@Slf4j
public class AccountController {

    private final UserService userService;
    private final AuthService authService;
    private final EmailOtpService emailOtpService;

    public AccountController(UserService userService, AuthService authService,
                             EmailOtpService emailOtpService) {
        this.userService = userService;
        this.authService = authService;
        this.emailOtpService = emailOtpService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@RequestBody UserRequest userRequest) {
        try {
            log.info("Registering user: {}", userRequest.getEmail());
            UserRespone createdUser = userService.Create(userRequest);

            emailOtpService.sendOtp(userRequest.getEmail());

            return new ResponseEntity<>(
                    new ApiResponse(true, "User registered successfully. Please verify your email with OTP."),
                    HttpStatus.CREATED
            );
        } catch (Exception e) {
            log.error("Registration failed: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse(false, "Registration failed: " + e.getMessage()));
        }
    }

    @PostMapping("/verify-registration")
    public ResponseEntity<ApiResponse> verifyRegistration(
            @Valid @RequestBody OtpVerificationRequest request) {
        try {
            VerificationResult result = emailOtpService.verifyOtp(request.getEmail(), request.getOtp());
            if (result.getStatus() != VerificationResult.valid().getStatus()) {
                // Activate the user account
                userService.activateUser(request.getEmail());
                return ResponseEntity.ok(new ApiResponse(true, "Account verified successfully"));
            }

            return ResponseEntity.badRequest().body(
                    new ApiResponse(false, "OTP verification failed: " + result.getStatus())
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse(false, "Verification failed: " + e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        log.info("Login attempt for user: {}", loginRequest.getEmail());
        LoginResponse response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/request-login-otp")
    public ResponseEntity<ApiResponse> requestLoginOtp(@RequestParam String email) {
        try {
            if (!userService.userExists(email)) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "User not found"));
            }

            emailOtpService.sendOtp(email);
            return ResponseEntity.ok(new ApiResponse(true, "OTP sent successfully"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse(false, "Failed to send OTP: " + e.getMessage()));
        }
    }

    @PostMapping("/login-with-otp")
    public ResponseEntity<LoginResponse> loginWithOtp(@Valid @RequestBody OtpLoginRequest request) {
        try {
            log.info("OTP login attempt for user: {}", request.getEmail());

            VerificationResult result = emailOtpService.verifyOtp(request.getEmail(), request.getOtp());

            if (result.getStatus() != VerificationResult.valid().getStatus()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new LoginResponse(false, "OTP verification failed: " + result.getStatus(), null));
            }
            LoginResponse response = authService.loginWithOtp(request.getEmail());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new LoginResponse(false, "Login failed: " + e.getMessage(), null));
        }
    }
}