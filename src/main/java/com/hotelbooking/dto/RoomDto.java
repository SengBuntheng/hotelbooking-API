package com.hotelbooking.dto;

import com.hotelbooking.Enum.RoomStatus;
import lombok.Data;

@Data
public class RoomDto {
    private Long id;
    private String roomNumber;
    private RoomStatus status;
    private Long roomTypeId;
    private String roomTypeName;
}