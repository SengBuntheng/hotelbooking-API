package com.hotelbooking.Config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class TelegramNotificationService {

    private String botToken;
    private String chatId;

    public void setBotToken(String botToken) {
        this.botToken = botToken;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public void sendMessage(String message) {
        try {
            URL url = new URL("https://api.telegram.org/bot" + botToken + "/sendMessage");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            String jsonInputString = "{\"chat_id\":\"" + chatId + "\",\"text\":\"" + message + "\"}";

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            try (InputStreamReader reader = new InputStreamReader(connection.getInputStream(), "utf-8")) {
                // Handle the response if needed
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Method to send animation (GIF)
    public void sendAnimation(String gifUrl) {
        try {
            URL url = new URL("https://api.telegram.org/bot" + botToken + "/sendAnimation");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            String jsonInputString = "{\"chat_id\":\"" + chatId + "\",\"animation\":\"" + gifUrl + "\"}";

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            try (InputStreamReader reader = new InputStreamReader(connection.getInputStream(), "utf-8")) {
                // Handle the response if needed
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
