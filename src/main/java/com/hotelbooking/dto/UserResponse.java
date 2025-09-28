package com.hotelbooking.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {
    private Long id;
    private String uuid;
    private String email;
    private String phone;
    private String username;
    private String firstName;
    private String lastName;
    private Boolean active;
    private String role;
    private String token;
    private LocalDateTime lastLogin;
    private LocalDateTime tokenExp;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
