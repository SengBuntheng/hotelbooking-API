package com.hotelbooking.service;

import com.hotelbooking.Config.EmailTemplateService;
import com.hotelbooking.Enum.VerificationResult;
import com.hotelbooking.dto.OtpDetails;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class EmailOtpService {

    private final JavaMailSender mailSender;
    private final EmailTemplateService templateService;
    private final OtpStorageService storageService;

    public EmailOtpService(JavaMailSender mailSender,
                           EmailTemplateService templateService,
                           OtpStorageService storageService) {
        this.mailSender = mailSender;
        this.templateService = templateService;
        this.storageService = storageService;
    }

    public void sendOtp(String email) throws Exception {
        String otp = generateOtp();
        String htmlContent = templateService.buildVerificationEmail(otp);

        // Store OTP
        storageService.storeOtp(email, otp);

        // Send Email
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(email);
        helper.setSubject("Your Verification Code");
        helper.setText(htmlContent, true); // true = HTML
        mailSender.send(message);
    }

    private String generateOtp() {
        return String.format("%06d", new Random().nextInt(999999));
    }

    public VerificationResult verifyOtp(String email, String otp) {
        OtpDetails otpDetails = storageService.getOtpDetails(email);

        if (otpDetails == null) {
            return VerificationResult.notFound();
        }

        if (otpDetails.getExpirationTime().isBefore(LocalDateTime.now())) {
            storageService.removeOtp(email);
            return VerificationResult.expired();
        }

        if (!otpDetails.getOtp().equals(otp)) {
            return VerificationResult.invalid();
        }

        storageService.removeOtp(email);
        return VerificationResult.valid();
    }
}
