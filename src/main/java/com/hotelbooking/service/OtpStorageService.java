package com.hotelbooking.service;

import com.hotelbooking.dto.OtpDetails;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpStorageService {
    private final Map<String, OtpDetails> otpStore = new ConcurrentHashMap<>();
    private final Duration otpExpiration = Duration.ofMinutes(5);

    public void storeOtp(String email, String otp) {
        OtpDetails otpDetails = new OtpDetails(
                email,
                otp,
                LocalDateTime.now().plus(otpExpiration)
        );
        otpStore.put(email, otpDetails);
    }

    public OtpDetails getOtpDetails(String email) {
        return otpStore.get(email);
    }

    public void removeOtp(String email) {
        otpStore.remove(email);
    }

    @Scheduled(fixedRate = 60000)
    public void cleanupExpiredOtps() {
        LocalDateTime now = LocalDateTime.now();
        otpStore.entrySet().removeIf(entry ->
                entry.getValue().getExpirationTime().isBefore(now)
        );
    }
}
