package com.hotelbooking.Config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

@Service
@Slf4j
public class TelegramNotificationService {

    private static final String TELEGRAM_API_URL = "https://api.telegram.org/bot";

    private final RestTemplate restTemplate;

    private String botToken;
    private String chatId;

    public TelegramNotificationService() {
        // Customize timeout
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(3000); // 3 sec
        requestFactory.setReadTimeout(3000);    // 3 sec
        this.restTemplate = new RestTemplate(requestFactory);
    }

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
            String response = restTemplate.postForObject(url, request, String.class);

            log.info("Telegram response: {}", response);  // Log the Telegram API response
        } catch (Exception e) {
            log.error("Failed to send Telegram notification.", e);
        }
    }


    private String escapeMarkdownV2(String text) {
        if (text == null) return "";
        return text
                .replace("\\", "\\\\")
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
}
