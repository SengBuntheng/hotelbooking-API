package com.hotelbooking.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class RoomTypeDto {
    private Long id;
    private String typeName;
    private String description;
    private BigDecimal pricePerNight;
    private Integer maxOccupancy;
    private Long hotelId;
}