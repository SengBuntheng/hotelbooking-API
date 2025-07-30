package com.hotelbooking.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class OtpDetails {
    private final String otp;
    private final LocalDateTime expirationTime;
    private int attempts = 0;

    public int incrementAttempts() {
        return ++this.attempts;
    }
}