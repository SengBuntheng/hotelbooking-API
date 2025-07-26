package com.hotelbooking.dto;

import lombok.*;

import java.time.LocalDateTime;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Data
public class OtpDetails {
    private String email;
    private String otp;
    private LocalDateTime expirationTime;
    private int attemptCount;



    public boolean isMaxAttemptsReached(int maxAttempts) {
        return attemptCount >= maxAttempts;
    }
}
