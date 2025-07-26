package com.hotelbooking.dto;

import lombok.*;
@Data
@NoArgsConstructor
public class LoginResponse {
        private boolean success;
        private String message;
        private String token;

        public LoginResponse(boolean success, String message, String token) {
                this.success = success;
                this.message = message;
                this.token = token;
        }
}