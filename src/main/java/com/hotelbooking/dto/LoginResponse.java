package com.hotelbooking.dto;

import com.hotelbooking.model.User;
import lombok.*;
import lombok.extern.java.Log;
import org.modelmapper.internal.bytebuddy.asm.Advice;
import org.springframework.http.HttpStatus;

import java.sql.Timestamp;

import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class LoginResponse {
    private String username;
    private Timestamp createDate;
    private Timestamp expDate;
    private Boolean active;
    private String email;
    private String token;
    private Timestamp tokenExp;
    private String phone;
    private UserRespone user;




}
