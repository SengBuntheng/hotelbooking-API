package com.hotelbooking.Config;

import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.Level;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class TelegramAppender extends AppenderBase<ILoggingEvent> {
    private final TelegramNotificationService telegramService = new TelegramNotificationService();
    private String token;
    private String chatId;

    public void setToken(String token) {
        this.token = token;
        this.telegramService.setBotToken(token);
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
        this.telegramService.setChatId(chatId);
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z")
                    .withZone(ZoneId.of("UTC"));

    private String environment = "prod";
    private boolean includeStacktrace = true;

    @Override
    protected void append(ILoggingEvent event) {
        if (isRateLimitExceeded()) {
            addInfo("Telegram rate limit exceeded. Skipping.");
            return;
        }

        try {
            String msg = formatMessage(event);
            telegramService.sendMessage(msg);
        } catch (Exception e) {
            addError("Telegram send failed", e);
        }
    }

    private String formatMessage(ILoggingEvent event) {
        String ts = TIME_FORMATTER.format(Instant.ofEpochMilli(event.getTimeStamp()));
        String host = getHostName();
        String pod = System.getenv("HOSTNAME") != null ? System.getenv("HOSTNAME") : host;
        String extIp = getExternalIp();
        LocationInfo loc = getLocationInfo(extIp);
        long uptime = getJvmUptime();
        MemoryStats mem = getMemoryStats();
        CpuStats cpu = getCpuStats();

        Map<String, String> mdc = event.getMDCPropertyMap();
        String requesterIp = mdc.getOrDefault("clientIp", "unknown");
        String requesterHost = mdc.getOrDefault("clientHost", "unknown");

        StringBuilder sb = new StringBuilder()
                .append(getEmojiForLevel(event.getLevel()))
                .append(" *").append(escapeMarkdown(environment.toUpperCase())).append(" ")
                .append(escapeMarkdown(event.getLevel().toString())).append("*\n\n")
                .append("*Time:* ").append(escapeMarkdown(ts)).append("\n")
                .append("*Pod:* ").append(escapeMarkdown(pod)).append("\n")
                .append("*Server IP:* ").append(escapeMarkdown(extIp)).append("\n");

        if (loc != null) {
            sb.append("*Location:* ").append(escapeMarkdown(loc.getCity())).append(", ")
                    .append(escapeMarkdown(loc.getCountry())).append("\n");
        }

        sb.append("*Uptime:* ").append(uptime).append(" sec\n")
                .append("*Heap:* ").append(mem.heapUsedMB).append("MB / ").append(mem.heapMaxMB).append("MB\n")
                .append("*CPU Load:* Proc: ").append(cpu.processLoad).append(", Sys: ")
                .append(cpu.systemLoad).append("\n")
                .append("*Requester IP:* ").append(escapeMarkdown(requesterIp)).append("\n")
                .append("*Requester Host:* ").append(escapeMarkdown(requesterHost)).append("\n\n")
                .append("*Message:*\n```")
                .append(escapeMarkdown(event.getFormattedMessage()))
                .append("```\n");

        if (includeStacktrace && event.getThrowableProxy() != null) {
            sb.append("\n*Stacktrace:*\n```")
                    .append(escapeMarkdown(event.getThrowableProxy().getClassName()))
                    .append(": ")
                    .append(escapeMarkdown(event.getThrowableProxy().getMessage()))
                    .append("\n  at ")
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

    // TODO: Implement helpers below properly
    private boolean isRateLimitExceeded() { return false; }
    private String getHostName() { return "api.bakongcity.city"; }
    private String getExternalIp() { return "api.bakongcity.city/ip"; } // Replace with actual external IP logic if needed
    private LocationInfo getLocationInfo(String ip) { return null; }
    private long getJvmUptime() { return 12345; }
    private MemoryStats getMemoryStats() { return new MemoryStats(256, 512); }
    private CpuStats getCpuStats() { return new CpuStats(0.3, 0.5); }
    private String getFirstStackTraceLine(ILoggingEvent event) { return "Line info here"; }

    private String getEmojiForLevel(Level level) {
        return switch (level.levelStr) {
            case "ERROR" -> "\uD83D\uDED1";
            case "WARN" -> "\u26A0\uFE0F";
            case "INFO" -> "\u2139\uFE0F";
            case "DEBUG" -> "\uD83D\uDD27";
            default -> "\u2753";
        };
    }

    // Placeholder classes
    public static class MemoryStats {
        public int heapUsedMB, heapMaxMB;
        public MemoryStats(int used, int max) {
            this.heapUsedMB = used;
            this.heapMaxMB = max;
        }
    }

    public static class CpuStats {
        public double processLoad, systemLoad;
        public CpuStats(double p, double s) {
            this.processLoad = p;
            this.systemLoad = s;
        }
    }

    public static class LocationInfo {
        public String getCity() { return "Phnom Penh"; }
        public String getCountry() { return "Cambodia"; }
    }
}
