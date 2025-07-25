package com.hotelbooking.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class MailTestController {

    @Autowired
    private JavaMailSender mailSender;

    @GetMapping("/mail-config")
    public String getMailConfig() {
        JavaMailSenderImpl sender = (JavaMailSenderImpl) mailSender;
        return String.format("""
            SMTP Configuration:
            Host: %s
            Port: %d
            Username: %s
            Protocol: %s
            Properties: %s
            """,
                sender.getHost(),
                sender.getPort(),
                sender.getUsername(),
                sender.getDefaultEncoding(),
                sender.getJavaMailProperties());
    }

    @GetMapping("/send-test")
    public String sendTestEmail() {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo("bunthengseng9@gmail.com");
            message.setSubject("SMTP Test");
            message.setText("This is a test email");
            mailSender.send(message);
            return "Test email sent!";
        } catch (Exception e) {
            return "Failed to send: " + e.getMessage();
        }
    }
}