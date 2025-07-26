package com.hotelbooking.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class OtpLoginRequest {
    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String otp;
}