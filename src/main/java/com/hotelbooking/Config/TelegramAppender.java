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

    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z")
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

    private String formatMessage(ILoggingEvent event) {
        StringBuilder sb = new StringBuilder();

        sb.append(getEnvironmentEmoji())
                .append(" *").append(environment.toUpperCase()).append(" ")
                .append(event.getLevel()).append("*\n");

        sb.append("🕒 ").append(TIME_FORMATTER.format(Instant.ofEpochMilli(event.getTimeStamp()))).append("\n");
        sb.append("📡 Server: ").append(getHostName()).append("\n");
        sb.append("📝 ").append(event.getFormattedMessage()).append("\n");

        if (includeStacktrace && event.getThrowableProxy() != null) {
            sb.append("\n```\n")
                    .append(event.getThrowableProxy().getClassName()).append(": ")
                    .append(event.getThrowableProxy().getMessage()).append("\n")
                    .append(event.getThrowableProxy().getStackTraceElementProxyArray()[0])
                    .append("\n```");
        }

        return sb.toString();
    }

    private String getEnvironmentEmoji() {
        return "prod".equalsIgnoreCase(environment) ? "🚨" : "⚠️";
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
            return "unknown";
        }
    }
}
