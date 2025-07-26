package com.hotelbooking.service;

import com.hotelbooking.dto.OtpDetails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class OtpStorageService {
    private final Map<String, OtpDetails> otpStore = new ConcurrentHashMap<>();
    private final ReentrantLock lock = new ReentrantLock();

    @Value("${otp.expiration.minutes:5}")
    private int otpExpirationMinutes;

    @Value("${otp.max.attempts:3}")
    private int maxAttempts;

    public void storeOtp(String email, String otp) {
        validateEmailAndOtp(email, otp);

        OtpDetails otpDetails = new OtpDetails(
                email,
                otp,
                LocalDateTime.now().plusMinutes(otpExpirationMinutes),
                0
        );

        lock.lock();
        try {
            otpStore.put(email, otpDetails);
        } finally {
            lock.unlock();
        }
    }
    public int getOtpExpirationMinutes() {
        return otpExpirationMinutes;
    }

    public Optional<OtpDetails> getOtpDetails(String email) {
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(otpStore.get(email));
    }

    public boolean incrementAndCheckAttempts(String email) {
        lock.lock();
        try {
            return getOtpDetails(email)
                    .map(details -> {
                        details.setAttemptCount(details.getAttemptCount() + 1);
                        if (details.getAttemptCount() >= maxAttempts) {
                            otpStore.remove(email);
                            return true;
                        }
                        return false;
                    })
                    .orElse(false);
        } finally {
            lock.unlock();
        }
    }

    public void removeOtp(String email) {
        if (email != null && !email.isBlank()) {
            lock.lock();
            try {
                otpStore.remove(email);
            } finally {
                lock.unlock();
            }
        }
    }

    public boolean isMaxAttemptsReached(String email) {
        return getOtpDetails(email)
                .map(details -> details.getAttemptCount() >= maxAttempts)
                .orElse(false);
    }

    @Scheduled(fixedRateString = "${otp.cleanup.interval.ms:60000}")
    public void cleanupExpiredOtps() {
        LocalDateTime now = LocalDateTime.now();
        lock.lock();
        try {
            otpStore.entrySet().removeIf(entry ->
                    entry.getValue() == null ||
                            entry.getValue().getExpirationTime().isBefore(now)
            );
        } finally {
            lock.unlock();
        }
    }

    public boolean hasOtp(String email) {
        return getOtpDetails(email).isPresent();
    }

    private void validateEmailAndOtp(String email, String otp) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        if (otp == null || otp.isBlank()) {
            throw new IllegalArgumentException("OTP cannot be null or empty");
        }
    }

    // For testing purposes
    void clearAllOtps() {
        lock.lock();
        try {
            otpStore.clear();
        } finally {
            lock.unlock();
        }
    }
}