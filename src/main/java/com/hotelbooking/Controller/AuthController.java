package com.hotelbooking.Controller;

import com.hotelbooking.dto.*;
import com.hotelbooking.service.AuthService;
import com.hotelbooking.service.EmailOtpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final EmailOtpService emailOtpService;

    public AuthController(AuthService authService, EmailOtpService emailOtpService) {
        this.authService = authService;
        this.emailOtpService = emailOtpService;
    }

    @PostMapping("/request-otp")
    public ResponseEntity<ApiResponse> requestOtp(@RequestParam String email) {
        try {
            emailOtpService.sendOtp(email);
            return ResponseEntity.ok(new ApiResponse(true, "OTP sent successfully"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse(false, "Failed to send OTP"));
        }
    }
}