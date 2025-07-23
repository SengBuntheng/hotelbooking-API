package com.hotelbooking.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import lombok.*;
import org.hibernate.query.sql.internal.ParameterRecognizerImpl;

import java.util.UUID;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserRespone {
    private UUID uuid;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String token;
}
