package com.hotelbooking.Config;

import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

@Service
public class EmailTemplateService {

    /**
     * Reads the otp-email-template.html file, replaces the placeholder
     * with the actual OTP, and returns the full HTML content as a string.
     *
     * @param otp The 6-digit one-time password.
     * @return The formatted HTML email content.
     * @throws IOException If the template file cannot be read.
     */
    public String buildVerificationEmail(String otp) throws IOException {
        // Load the template from the classpath resources
        String template = readTemplateFile("/templates/email-verification.html");

        // Replace the placeholder with the actual OTP
        return template.replace("{{otp}}", otp);
    }

    private String readTemplateFile(String path) throws IOException {
        try (Reader reader = new InputStreamReader(
                this.getClass().getResourceAsStream(path), StandardCharsets.UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        } catch (Exception e) {
            throw new IOException("Could not read email template file: " + path, e);
        }
    }
}