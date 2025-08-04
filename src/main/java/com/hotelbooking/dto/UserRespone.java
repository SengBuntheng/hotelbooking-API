package com.hotelbooking.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.hotelbooking.model.User;
import lombok.*;

import java.sql.Timestamp;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserRespone {
    private UUID uuid;
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private String phone;

    private Timestamp createdAt;

    private String token;
    private Timestamp tokenExp;

    private Integer code;
    private String message;


}
