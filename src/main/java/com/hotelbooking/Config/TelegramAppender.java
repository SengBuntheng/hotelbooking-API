package com.hotelbooking.Config;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.client.RestTemplate;

import java.net.InetAddress;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
@Getter
@Setter
public class TelegramAppender extends AppenderBase<ILoggingEvent> {
    private String token;
    private String chatId;
    private String environment = "prod";
    private boolean includeStacktrace = true;
    private int rateLimit = 5;

    private final RestTemplate restTemplate = new RestTemplate();
    private final AtomicInteger messageCount = new AtomicInteger(0);
    private long lastResetTime = System.currentTimeMillis();
    private static final Marker NO_TELEGRAM = MarkerFactory.getMarker("NO_TELEGRAM");

    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm:ss")
                    .withZone(ZoneId.systemDefault());

    @Override
    protected void append(ILoggingEvent event) {
        if (isRateLimitExceeded()) return;

        try {
            String message = formatMessage(event);
            String url = String.format(
                    "https://api.telegram.org/bot%s/sendMessage?chat_id=%s&text=%s&parse_mode=Markdown",
                    token,
                    chatId,
                    URLEncoder.encode(message, StandardCharsets.UTF_8)
            );
            restTemplate.getForObject(url, String.class);
            messageCount.incrementAndGet();
        } catch (Exception e) {
            addError("Failed to send log to Telegram", e);
        }
    }
    private String getAppName() {
        try {
            return System.getProperty("spring.application.name", "hotel-booking");
        } catch (Exception e) {
            return "unknown-app";
        }
    }
    private String formatMessage(ILoggingEvent event) {
        StringBuilder sb = new StringBuilder();

        // Header with animated emoji
        sb.append(getErrorHeader(event.getLevel().levelStr))
                .append("\n\n");

        // Main error content
        sb.append("? *Time:* ").append(TIME_FORMATTER.format(Instant.ofEpochMilli(event.getTimeStamp()))).append("\n");
        sb.append("??? *Server:* ").append(getHostName()).append("\n\n");
        sb.append("?? *Message:*\n").append("```\n").append(event.getFormattedMessage()).append("\n```\n\n");
        sb.append("?? *App:* ").append(getAppName()).append("\n");
        // Stacktrace if enabled
        if (includeStacktrace && event.getThrowableProxy() != null) {
            sb.append("?? *Stacktrace:*\n```\n")
                    .append(event.getThrowableProxy().getClassName()).append(": ")
                    .append(event.getThrowableProxy().getMessage()).append("\n")
                    .append(getFirstStackTraceLine(event))
                    .append("\n```");
        }

        return sb.toString();
    }

    private String getErrorHeader(String level) {
        String emojiAnimation = switch (level.toLowerCase()) {
            case "error" -> "??????";
            case "warn" -> "????";
            default -> "??";
        };

        return String.format("%s *%s %s ERROR* %s",
                emojiAnimation,
                environment.toUpperCase(),
                level.toUpperCase(),
                emojiAnimation);
    }

    private String getFirstStackTraceLine(ILoggingEvent event) {
        if (event.getThrowableProxy().getStackTraceElementProxyArray() == null ||
                event.getThrowableProxy().getStackTraceElementProxyArray().length == 0) {
            return "No stacktrace available";
        }
        return event.getThrowableProxy().getStackTraceElementProxyArray()[0].toString();
    }

    private boolean isRateLimitExceeded() {
        long now = System.currentTimeMillis();
        if (now - lastResetTime > TimeUnit.MINUTES.toMillis(1)) {
            messageCount.set(0);
            lastResetTime = now;
        }
        return messageCount.get() >= rateLimit;
    }

    private String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName().split("\\.")[0];
        } catch (Exception e) {
            return "unknown-host";
        }
    }
}