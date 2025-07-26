package com.hotelbooking.dto;


import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@Data
@ToString
@Getter
@Setter
@Builder
public class LoginRequest {
    private String email;
    private String password;
    private String otp;


}
