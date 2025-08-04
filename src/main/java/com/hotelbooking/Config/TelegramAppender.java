package com.hotelbooking.Config;

import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.Level;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Custom Logback appender that sends log events as formatted Telegram messages.
 * Supports configurable bot token, chat ID, environment tagging, and stacktrace inclusion.
 */
public class TelegramAppender extends AppenderBase<ILoggingEvent> {

    private TelegramNotificationService telegramService;
    private String token;
    private String chatId;
    private String environment = "prod";
    private boolean includeStacktrace = true;

    private static final String JAPAN_ALERT_GIF = "https://media.giphy.com/media/xT0xeJpnrWC4XWblEk/giphy.gif";
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss z")
            .withZone(ZoneId.of("UTC"));

    public TelegramAppender() {
        // Initialize TelegramNotificationService once when the appender is created
        this.telegramService = new TelegramNotificationService();
    }

    // Configuration setters

    public void setToken(String token) {
        this.token = token;
        this.telegramService.setBotToken(token);
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
        this.telegramService.setChatId(chatId);
    }

    public void setEnvironment(String environment) {
        if (environment != null && !environment.isBlank()) {
            this.environment = environment.trim();
        }
    }

    public void setIncludeStacktrace(boolean includeStacktrace) {
        this.includeStacktrace = includeStacktrace;
    }

    @Override
    protected void append(ILoggingEvent event) {
        if (isRateLimitExceeded()) {
            addInfo("Telegram rate limit exceeded. Skipping log notification.");
            return;
        }

        try {
            // Send GIF if the log level is ERROR or WARN
            if (event.getLevel().isGreaterOrEqual(Level.WARN)) {
                telegramService.sendAnimation(JAPAN_ALERT_GIF);
            }

            String message = formatMessage(event);
            telegramService.sendMessage(message);
        } catch (Exception ex) {
            addError("Failed to send Telegram notification", ex);
        }
    }

    private String formatMessage(ILoggingEvent event) {
        String timestamp = TIME_FORMATTER.format(Instant.ofEpochMilli(event.getTimeStamp()));
        String podName = System.getenv().getOrDefault("HOSTNAME", getHostName());
        String serverIp = getExternalIp();
        LocationInfo location = getLocationInfo(serverIp);
        long uptimeSec = getJvmUptimeSeconds();
        MemoryStats memory = getMemoryStats();
        CpuStats cpu = getCpuStats();

        Map<String, String> mdc = event.getMDCPropertyMap();
        String requesterIp = mdc.getOrDefault("clientIp", "unknown");
        String requesterHost = mdc.getOrDefault("clientHost", "unknown");

        StringBuilder sb = new StringBuilder();

        sb.append(getEmojiForLevel(event.getLevel()))
                .append(" *").append(escapeMarkdown(environment.toUpperCase())).append(" ")
                .append(escapeMarkdown(event.getLevel().toString())).append("*\n\n")
                .append("*Time:* ").append(escapeMarkdown(timestamp)).append("\n")
                .append("*Pod:* ").append(escapeMarkdown(podName)).append("\n")
                .append("*Server IP:* ").append(escapeMarkdown(serverIp)).append("\n");

        if (location != null) {
            sb.append("*Location:* ")
                    .append(escapeMarkdown(location.getCity())).append(", ")
                    .append(escapeMarkdown(location.getCountry()))
                    .append("\n");
        }

        sb.append("*Uptime:* ").append(uptimeSec).append(" sec\n")
                .append("*Heap:* ").append(memory.heapUsedMB).append("MB / ").append(memory.heapMaxMB).append("MB\n")
                .append("*CPU Load:* Proc: ").append(String.format("%.2f", cpu.processLoad))
                .append(", Sys: ").append(String.format("%.2f", cpu.systemLoad)).append("\n")
                .append("*Requester IP:* ").append(escapeMarkdown(requesterIp)).append("\n")
                .append("*Requester Host:* ").append(escapeMarkdown(requesterHost)).append("\n\n")
                .append("*Message:*\n```")
                .append(escapeMarkdown(event.getFormattedMessage()))
                .append("```\n");

        if (includeStacktrace && event.getThrowableProxy() != null) {
            sb.append("\n*Stacktrace:*\n```")
                    .append(escapeMarkdown(event.getThrowableProxy().getClassName())).append(": ")
                    .append(escapeMarkdown(event.getThrowableProxy().getMessage())).append("\n  at ")
                    .append(escapeMarkdown(getFirstStackTraceLine(event)))
                    .append("```\n");
        }

        return sb.toString();
    }

    private String escapeMarkdown(String text) {
        if (text == null) return "";
        return text
                .replace("_", "\\_")
                .replace("*", "\\*")
                .replace("[", "\\[")
                .replace("]", "\\]")
                .replace("(", "\\(")
                .replace(")", "\\)")
                .replace("~", "\\~")
                .replace("`", "\\`")
                .replace(">", "\\>")
                .replace("#", "\\#")
                .replace("+", "\\+")
                .replace("-", "\\-")
                .replace("=", "\\=")
                .replace("|", "\\|")
                .replace("{", "\\{")
                .replace("}", "\\}")
                .replace(".", "\\.")
                .replace("!", "\\!");
    }

    private boolean isRateLimitExceeded() {
        return false;
    }

    private String getHostName() {
        return "api.bakongcity.city";
    }

    private String getExternalIp() {
        return "api.bakongcity.city/ip";
    }

    private LocationInfo getLocationInfo(String ip) {
        return new LocationInfo("Phnom Penh", "Cambodia");
    }

    private long getJvmUptimeSeconds() {
        return 12345L;
    }

    private MemoryStats getMemoryStats() {
        return new MemoryStats(256, 512);
    }

    private CpuStats getCpuStats() {
        return new CpuStats(0.3, 0.5);
    }

    private String getFirstStackTraceLine(ILoggingEvent event) {
        return "com.hotelbooking.SomeClass.method(SomeClass.java:123)";
    }

    private String getEmojiForLevel(Level level) {
        switch (level.levelStr) {
            case "ERROR": return "\uD83D\uDED1"; // 🚑
            case "WARN":  return "\u26A0\uFE0F"; // ⚠️
            case "INFO":  return "\u2139\uFE0F"; // ℹ️
            case "DEBUG": return "\uD83D\uDD27"; // 🛧
            default:      return "\u2753";       // ❓
        }
    }

    // Helper classes for structured stats
    public static class MemoryStats {
        public final int heapUsedMB;
        public final int heapMaxMB;
        public MemoryStats(int heapUsedMB, int heapMaxMB) {
            this.heapUsedMB = heapUsedMB;
            this.heapMaxMB = heapMaxMB;
        }
    }

    public static class CpuStats {
        public final double processLoad;
        public final double systemLoad;
        public CpuStats(double processLoad, double systemLoad) {
            this.processLoad = processLoad;
            this.systemLoad = systemLoad;
        }
    }

    public static class LocationInfo {
        private final String city;
        private final String country;

        public LocationInfo(String city, String country) {
            this.city = city;
            this.country = country;
        }

        public String getCity() { return city; }
        public String getCountry() { return country; }
    }
}
