package com.hotelbooking.Config;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class EmailTemplateService {

    public String buildVerificationEmail(String otpCode) throws IOException {
        // Load HTML template from resources
        ClassPathResource resource = new ClassPathResource("templates/email-verification.html");
        String template = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);

        // Replace placeholders
        return template.replace("{{OTP_CODE}}", otpCode)
                .replace("{{EXPIRY_MINUTES}}", "5");
    }
}