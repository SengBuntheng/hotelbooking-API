package com.hotelbooking.Config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.InetAddress;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
@Setter
public class TelegramAppender extends AppenderBase<ILoggingEvent> {
    private String token;
    private String chatId;
    private String environment = "prod";
    private boolean includeStacktrace = true;
    private int rateLimit = 5; // Messages per minute

    // A professional, animated error icon for your logs
    private String errorImageUrl = "https://media1.tenor.com/m/Kr6jKur1_DYAAAAd/cat.gif";

    private final RestTemplate restTemplate = new RestTemplate();
    private final AtomicInteger messageCount = new AtomicInteger(0);
    private long lastResetTime = System.currentTimeMillis();

    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z")
                    .withZone(ZoneId.of("UTC")); // Use UTC for consistency

    @Override
    protected void append(ILoggingEvent event) {
        if (isRateLimitExceeded()) {
            addInfo("Telegram rate limit exceeded. Log message skipped.");
            return;
        }

        try {
            String message = formatMessage(event);

            // CONDITIONAL LOGIC: Send a photo for errors, text for everything else
            if (event.getLevel().equals(Level.ERROR)) {
                sendTelegramPhoto(message);
            } else {
                sendTelegramMessage(message);
            }

            messageCount.incrementAndGet();
        } catch (Exception e) {
            addError("Failed to send log to Telegram", e);
        }
    }

    /**
     * Sends a plain text message to Telegram. Used for INFO, WARN, etc.
     */
    private void sendTelegramMessage(String message) {
        String url = "https://api.telegram.org/bot" + token + "/sendMessage";

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("chat_id", chatId);
        body.add("text", message);
        body.add("parse_mode", "Markdown");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        restTemplate.postForObject(url, request, String.class);
    }

    /**
     * Sends a message with an animated photo. Used for ERROR level logs.
     */
    private void sendTelegramPhoto(String caption) {
        String url = "https://api.telegram.org/bot" + token + "/sendPhoto";

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("chat_id", chatId);
        body.add("photo", errorImageUrl); // The URL to the animated error icon
        body.add("caption", caption);
        body.add("parse_mode", "Markdown");

        HttpHeaders headers = new HttpHeaders();
        // Corrected Content-Type for this kind of request with RestTemplate
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        restTemplate.postForObject(url, request, String.class);
    }

    private String formatMessage(ILoggingEvent event) {
        StringBuilder sb = new StringBuilder();

        sb.append(getEmojiForLevel(event.getLevel())).append(" *")
                .append(environment.toUpperCase()).append(" ")
                .append(event.getLevel()).append("*\n\n");

        sb.append("*Time:* `").append(TIME_FORMATTER.format(Instant.ofEpochMilli(event.getTimeStamp()))).append("`\n");
        sb.append("*Server:* `").append(getHostName()).append("`\n\n");
        sb.append("*Message:*\n").append(event.getFormattedMessage()).append("\n");

        if (includeStacktrace && event.getThrowableProxy() != null) {
            sb.append("\n*Stacktrace:*\n");
            sb.append("```\n")
                    .append(event.getThrowableProxy().getClassName()).append(": ")
                    .append(event.getThrowableProxy().getMessage()).append("\n")
                    .append("  at ").append(getFirstStackTraceLine(event))
                    .append("\n```");
        }

        return sb.toString();
    }

    private String getFirstStackTraceLine(ILoggingEvent event) {
        if (event.getThrowableProxy().getStackTraceElementProxyArray() == null ||
                event.getThrowableProxy().getStackTraceElementProxyArray().length == 0) {
            return "No stacktrace available";
        }
        return event.getThrowableProxy().getStackTraceElementProxyArray()[0].toString();
    }

    private String getEmojiForLevel(Level level) {
        if (level.equals(Level.ERROR)) {
            return "🚨"; // Red alert for errors
        } else if (level.equals(Level.WARN)) {
            return "⚠️"; // Warning sign
        } else if (level.equals(Level.INFO)) {
            return "ℹ️"; // Info sign
        } else {
            return "⚙️"; // Gear for DEBUG/TRACE
        }
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
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "unknown-host";
        }
    }
}
