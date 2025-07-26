package com.hotelbooking.Controller;

import com.hotelbooking.Enum.VerificationResult;
import com.hotelbooking.dto.*;
import com.hotelbooking.service.EmailOtpService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/otp")
public class OtpController {

    private final EmailOtpService emailOtpService;

    public OtpController(EmailOtpService emailOtpService) {
        this.emailOtpService = emailOtpService;
    }

    @PostMapping("/send")
    @Operation(summary = "Send OTP to email")
    public ResponseEntity<ApiResponse> sendOtp(
            @RequestParam @Email String email) {
        try {
            emailOtpService.sendOtp(email);
            return ResponseEntity.ok(new ApiResponse(true, "OTP sent successfully to " + email));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    new ApiResponse(false, "Failed to send OTP: " + e.getMessage()));
        }
    }

    @PostMapping("/verify")
    @Operation(summary = "Verify OTP")
    public ResponseEntity<ApiResponse> verifyOtp(
            @Valid @RequestBody OtpVerificationRequest request) {
        try {
            VerificationResult result = emailOtpService.verifyOtp(request.getEmail(), request.getOtp());

            return switch (result.getStatus()) {
                case VALID -> ResponseEntity.ok(new ApiResponse(true, "OTP verified successfully"));
                case INVALID -> ResponseEntity.badRequest().body(new ApiResponse(false, "Invalid OTP code"));
                case EXPIRED -> ResponseEntity.badRequest().body(new ApiResponse(false, "OTP has expired"));
                case NOT_FOUND -> ResponseEntity.badRequest().body(new ApiResponse(false, "No OTP found for this email"));
            };
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    new ApiResponse(false, "Verification failed: " + e.getMessage()));
        }
    }
}