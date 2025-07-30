package com.hotelbooking.Config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@Slf4j
public class TelegramNotificationService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${TELEGRAM_BOT_TOKEN}")
    private String botToken;

    @Value("${TELEGRAM_CHAT_ID}")
    private String chatId;

    /**
     * Sends a simple text message to the configured Telegram chat.
     * @param message The text to send. Supports Markdown.
     */
    public void sendMessage(String message) {
        if (botToken == null || chatId == null || botToken.isEmpty() || chatId.isEmpty()) {
            log.warn("Telegram bot token or chat ID is not configured. Skipping notification.");
            return;
        }

        try {
            String url = UriComponentsBuilder.fromHttpUrl("https://api.telegram.org/bot" + botToken + "/sendMessage")
                    .queryParam("chat_id", chatId)
                    .queryParam("text", message)
                    .queryParam("parse_mode", "Markdown")
                    .toUriString();

            restTemplate.getForObject(url, String.class);
        } catch (Exception e) {
            log.error("Failed to send Telegram notification.", e);
        }
    }
}
