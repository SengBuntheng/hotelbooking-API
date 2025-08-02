package com.hotelbooking.Config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class TelegramNotificationService {

    private final RestTemplate restTemplate = new RestTemplate();

    private String botToken;
    private String chatId;

    public void setBotToken(String botToken) {
        this.botToken = botToken;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public void sendMessage(String message) {
        if (botToken == null || chatId == null || botToken.isEmpty() || chatId.isEmpty()) {
            log.warn("Telegram bot token or chat ID is not configured. Skipping notification.");
            return;
        }

        try {
            String url = "https://api.telegram.org/bot" + botToken + "/sendMessage";

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("chat_id", chatId);
            body.add("text", message);
            body.add("parse_mode", "Markdown");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
            restTemplate.postForObject(url, request, String.class);
        } catch (Exception e) {
            log.error("Failed to send Telegram notification.", e);
        }
    }
}
