package com.hotelbooking.dto;

import lombok.*;

import java.time.LocalDateTime;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class OtpDetails {
    private String email;
    private String otp;
    private LocalDateTime expirationTime;
}