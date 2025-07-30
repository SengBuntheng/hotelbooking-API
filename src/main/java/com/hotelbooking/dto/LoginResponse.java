package com.hotelbooking.dto;

import lombok.*;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {
        private String token;

        @Builder.Default
        private String tokenType = "Bearer";

        private UserRespone user;
}