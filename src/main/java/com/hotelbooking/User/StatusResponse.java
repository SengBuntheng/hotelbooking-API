package com.hotelbooking.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter @Setter
@AllArgsConstructor
public class StatusResponse {

    int code;

    String message;

    public StatusResponse() {}

    public StatusResponse successResponse() {
        StatusResponse response = new StatusResponse();

        response.setCode(HttpStatus.OK.value());
        response.setMessage("Success.");

        return response;
    }

    public StatusResponse errorResponse(String message) {
        StatusResponse response = new StatusResponse();

        response.setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.setMessage(message);

        return response;
    }


}