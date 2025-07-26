package com.hotelbooking.dto;


import lombok.*;

@AllArgsConstructor
@Data
@ToString
@Getter
@Setter
@Builder
public class LoginRequest {
    private String email;
    private String password;


}
