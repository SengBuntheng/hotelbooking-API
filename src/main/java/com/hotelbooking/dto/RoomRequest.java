package com.hotelbooking.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hotelbooking.Enum.RoomStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RoomRequest {

    @JsonProperty("roomId")
    private String roomNumber;

    @JsonProperty("Status")
    private RoomStatus status;

    @JsonProperty("type")
    private Long roomTypeId;
}
