package com.hotelbooking.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import lombok.*;

import java.util.Locale;
@Builder
@Data
@ToString
@Getter
@AllArgsConstructor
@Setter
@NoArgsConstructor
public class UserRequest {
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private String phone;
    private String password;

}
