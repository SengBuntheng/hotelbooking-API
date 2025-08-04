package com.hotelbooking.dto;


import lombok.*;
@ToString
@Getter
@Setter
@Builder
public class LoginRequest {
    private String email;
    private String password;


}
