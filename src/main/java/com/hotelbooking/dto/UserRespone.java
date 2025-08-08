package com.hotelbooking.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.sql.Date;
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
    private Long id;
    private UUID uuid;
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private String phone;
    private Date Dob;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private String createdBy;
    private String updatedBy;
    private Boolean active;
    private String role;
    private String password;
    private Timestamp lastLogin;
    private String token;
    private Timestamp tokenExp;
    private Integer code;
    private String message;

}
