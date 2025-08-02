package com.hotelbooking.Config;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Component
@Profile("prod")
@Slf4j
public class ApplicationLifecycleNotifier {

    private final TelegramNotificationService telegramService;
    private final String serverHostName;

    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z")
                    .withZone(ZoneId.of("UTC"));

    @Autowired
    public ApplicationLifecycleNotifier(TelegramNotificationService telegramService) {
        this.telegramService = telegramService;
        this.serverHostName = getHostName();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("Application has started. Sending startup notification to Telegram.");
        String message = String.format(
                "\u2705 *Application Started*\n\n" +
                        "*Time:* `%s`\n" +
                        "*Server:* `%s`\n\n" +
                        "The service is now online and operational.",
                TIME_FORMATTER.format(Instant.now()),
                serverHostName
        );
        telegramService.sendMessage(message);
    }

    @PreDestroy
    public void onShutdown() {
        log.info("Application is shutting down. Sending shutdown notification to Telegram.");
        String message = String.format(
                "\uD83D\uDEA8 *Application Shutting Down*\n\n" +
                        "*Time:* `%s`\n" +
                        "*Server:* `%s`\n\n" +
                        "The service is shutting down gracefully.",
                TIME_FORMATTER.format(Instant.now()),
                serverHostName
        );
        telegramService.sendMessage(message);
    }

    private String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            log.error("Could not determine hostname.", e);
            return "unknown-host";
        }
    }
}
