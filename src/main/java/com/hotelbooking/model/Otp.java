package com.hotelbooking.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "otp_details")
@Data
@NoArgsConstructor
public class Otp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String otpCode;

    @Column(nullable = false)
    private LocalDateTime expirationTime;

    @Column(nullable = false)
    private int attempts = 0;

    public Otp(String email, String otpCode, LocalDateTime expirationTime) {
        this.email = email;
        this.otpCode = otpCode;
        this.expirationTime = expirationTime;
    }

    public int incrementAttempts() {
        return ++this.attempts;
    }
}
