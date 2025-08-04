package com.hotelbooking.Enum;

import com.hotelbooking.Enum.VerificationResult;

public interface emailOtpService {
    VerificationResult verifyOtp(String email, String otp);
}