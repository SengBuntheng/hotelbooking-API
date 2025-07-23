package com.hotelbooking.dto;

import lombok.*;
@Data
@NoArgsConstructor
public class LoginResponse {
        private String token;
        private String tokenType = "Bearer";
        private UserRespone user;
}
