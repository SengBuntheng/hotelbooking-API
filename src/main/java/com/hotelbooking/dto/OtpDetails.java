package com.hotelbooking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class OtpDetails {
    private String email;
    private String otp;
    private LocalDateTime expirationTime;
}