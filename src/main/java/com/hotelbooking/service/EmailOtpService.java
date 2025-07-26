package com.hotelbooking.service;

import com.hotelbooking.Config.EmailTemplateService;
import com.hotelbooking.Enum.VerificationResult;
import com.hotelbooking.dto.OtpDetails;
import com.hotelbooking.exception.OtpException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class EmailOtpService {
    private static final Logger logger = LoggerFactory.getLogger(EmailOtpService.class);
    private static final int OTP_LENGTH = 6;
    private static final int MAX_OTP_VALUE = 999999;

    private final JavaMailSender mailSender;
    private final EmailTemplateService templateService;
    private final OtpStorageService storageService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${otp.resend.timeout.minutes:1}")
    private long resendTimeoutMinutes;

    public EmailOtpService(JavaMailSender mailSender,
                           EmailTemplateService templateService,
                           OtpStorageService storageService) {
        this.mailSender = mailSender;
        this.templateService = templateService;
        this.storageService = storageService;
    }

    public void sendOtp(String email) throws OtpException, IOException {
        validateEmail(email);
        checkResendTimeout(email);

        String otp = generateSecureOtp();
        String htmlContent = templateService.buildVerificationEmail(otp);

        try {
            sendEmail(email, htmlContent);
            storageService.storeOtp(email, otp);
            logger.info("OTP sent successfully to {}", email);
        } catch (MessagingException e) {
            logger.error("Failed to send OTP email to {}", email, e);
            throw new OtpException("Failed to send OTP email", e);
        }
    }

    public VerificationResult verifyOtp(String email, String otp) {
        validateEmail(email);
        validateOtp(otp);

        Optional<OtpDetails> otpDetails = storageService.getOtpDetails(email);

        if (otpDetails.isEmpty()) {
            return VerificationResult.notFound();
        }

        OtpDetails details = otpDetails.get();

        if (details.getExpirationTime().isBefore(LocalDateTime.now())) {
            storageService.removeOtp(email);
            return VerificationResult.expired();
        }

        if (storageService.incrementAndCheckAttempts(email)) {
            return VerificationResult.expired();
        }

        if (!details.getOtp().equals(otp)) {
            return VerificationResult.invalid();
        }

        storageService.removeOtp(email);
        return VerificationResult.valid();
    }

    private void sendEmail(String email, String content) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(email);
        helper.setSubject("Your Verification Code");
        helper.setText(content, true);
        mailSender.send(message);
    }

    private String generateSecureOtp() {
        return String.format("%06d", secureRandom.nextInt(MAX_OTP_VALUE + 1));
    }

    private void validateEmail(String email) {
        if (!StringUtils.hasText(email)) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        // Add more email validation if needed
    }

    private void validateOtp(String otp) {
        if (!StringUtils.hasText(otp)) {
            throw new IllegalArgumentException("OTP cannot be empty");
        }
        if (otp.length() != OTP_LENGTH) {
            throw new IllegalArgumentException("OTP must be " + OTP_LENGTH + " digits");
        }
        try {
            Integer.parseInt(otp);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("OTP must contain only digits");
        }
    }

    private void checkResendTimeout(String email) throws OtpException {
        Optional<OtpDetails> existingOtp = storageService.getOtpDetails(email);
        if (existingOtp.isPresent()) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime createdTime = existingOtp.get().getExpirationTime()
                    .minusMinutes(storageService.getOtpExpirationMinutes());

            long minutesSinceLastOtp = Duration.between(createdTime, now).toMinutes();

            if (minutesSinceLastOtp < resendTimeoutMinutes) {
                long secondsToWait = TimeUnit.MINUTES.toSeconds(resendTimeoutMinutes - minutesSinceLastOtp);
                throw new OtpException(String.format(
                        "Please wait %d more seconds before requesting a new OTP",
                        secondsToWait
                ));
            }
        }
    }
}