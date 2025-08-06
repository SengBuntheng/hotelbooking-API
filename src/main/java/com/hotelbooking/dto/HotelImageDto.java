package com.hotelbooking.dto;

import lombok.Data;

@Data
public class HotelImageDto {
    private Long id;
    private String imageUrl;
    private String imageType;
    private Long hotelId;
}