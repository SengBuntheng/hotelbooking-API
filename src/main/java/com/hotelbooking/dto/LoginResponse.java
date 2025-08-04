package com.hotelbooking.dto;

import com.hotelbooking.model.User;
import lombok.*;
import lombok.extern.java.Log;
import org.modelmapper.internal.bytebuddy.asm.Advice;
import org.springframework.http.HttpStatus;

import java.sql.Timestamp;
// In LoginResponse.java
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class LoginResponse extends StatusResponse {
    private String username;
    private Timestamp createDate;
    private Timestamp expDate;
    private Boolean active;
    private String token;
    private Timestamp tokenExp;

    private UserRespone user;
    public static LoginResponse responseError(HttpStatus status, String message) {
        return LoginResponse.builder()
                .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message(message)
                .build();
    }

}
