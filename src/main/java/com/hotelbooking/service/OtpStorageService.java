package com.hotelbooking.service;

import com.hotelbooking.Repository.OtpRepository;
import com.hotelbooking.dto.OtpDetails;
import com.hotelbooking.model.Otp;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class OtpStorageService {

    private final OtpRepository otpRepository;
    private final int maxAttempts;

    @Value("${otp.expiration.minutes:5}")
    private long otpExpirationMinutes;

    public OtpStorageService(OtpRepository otpRepository, @Value("${otp.max.attempts:5}") int maxAttempts) {
        this.otpRepository = otpRepository;
        this.maxAttempts = maxAttempts;
    }

    @Transactional
    public void storeOtp(String key, String otpCode) {
        LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(otpExpirationMinutes);

        Otp otp = otpRepository.findByEmail(key).orElse(new Otp(key, otpCode, expirationTime));

        otp.setOtpCode(otpCode);
        otp.setExpirationTime(expirationTime);
        otp.setAttempts(0);

        otpRepository.save(otp);
    }

    public Optional<OtpDetails> getOtpDetails(String key) {
        return otpRepository.findByEmail(key)
                .map(otp -> new OtpDetails(otp.getOtpCode(), otp.getExpirationTime()));
    }

    @Transactional
    public void removeOtp(String key) {
        otpRepository.findByEmail(key).ifPresent(otpRepository::delete);
    }

    public long getOtpExpirationMinutes() {
        return otpExpirationMinutes;
    }

    @Transactional
    public boolean incrementAndCheckAttempts(String key) {
        Optional<Otp> otpOptional = otpRepository.findByEmail(key);
        if (otpOptional.isPresent()) {
            Otp otp = otpOptional.get();
            int newAttempts = otp.incrementAttempts();
            otpRepository.save(otp);

            if (newAttempts >= maxAttempts) {
                otpRepository.delete(otp);
                return true;
            }
        }
        return false; // Still within attempt limit or OTP not found
    }
}
