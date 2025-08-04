// StatusResponse.java
package com.hotelbooking.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class StatusResponse {
    private int code;
    private String message;

    public StatusResponse successResponse() {
        return StatusResponse.builder()
                .code(HttpStatus.OK.value())
                .message("Success.")
                .build();
    }

    public StatusResponse errorResponse(String message) {
        return StatusResponse.builder()
                .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message(message)
                .build();
    }
}
