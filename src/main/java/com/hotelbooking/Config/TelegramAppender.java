package com.hotelbooking.Config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.client.RestTemplate;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
public class TelegramAppender extends AppenderBase<ILoggingEvent> {
    private String token;
    private String chatId;
    private boolean includeStacktrace;
    private int rateLimit;
    private String environment;
    private final RestTemplate restTemplate = new RestTemplate();
    private final AtomicInteger messageCount = new AtomicInteger(0);
    private long lastResetTime = System.currentTimeMillis();
    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z")
                    .withZone(ZoneId.systemDefault());

    @Override
    protected void append(ILoggingEvent event) {
        if (!isRateLimitExceeded()) {
            try {
                String formattedMessage = formatMessage(event);
                String url = String.format(
                        "https://api.telegram.org/bot%s/sendMessage?chat_id=%s&text=%s&parse_mode=Markdown",
                        token,
                        chatId,
                        URLEncoder.encode(formattedMessage, StandardCharsets.UTF_8)
                );

                restTemplate.getForObject(url, String.class);
                messageCount.incrementAndGet();
            } catch (Exception e) {
                addError("Failed to send log to Telegram", e);
            }
        }
    }

    private String formatMessage(ILoggingEvent event) {
        StringBuilder sb = new StringBuilder();

        // Environment and level emoji
        sb.append(getEnvironmentEmoji())
                .append(" *").append(environment).append(" ")
                .append(event.getLevel().toString()).append("*\n");

        // Timestamp
        sb.append("🕒 ")
                .append(TIME_FORMATTER.format(Instant.ofEpochMilli(event.getTimeStamp())))
                .append("\n");


        // Message
        sb.append("📝 ").append(event.getFormattedMessage()).append("\n");

        // Stacktrace if enabled
        if (includeStacktrace && event.getThrowableProxy() != null) {
            sb.append("\n```\n")
                    .append(event.getThrowableProxy().getStackTraceElementProxyArray()[0])
                    .append("\n```");
        }

        // Additional context for production
        if ("prod".equals(environment)) {
            sb.append("\n🔗 _TraceID: ").append(event.getMDCPropertyMap().getOrDefault("traceId", "none"))
                    .append("_");
        }

        return sb.toString();
    }

    private String getEnvironmentEmoji() {
        return "prod".equals(environment) ? "🚨" : "⚠️";
    }

    private boolean isRateLimitExceeded() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastResetTime > TimeUnit.MINUTES.toMillis(1)) {
            messageCount.set(0);
            lastResetTime = currentTime;
        }
        return messageCount.get() >= rateLimit;
    }
}