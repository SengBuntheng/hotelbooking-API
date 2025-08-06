package com.hotelbooking.dto;

import lombok.Data;

@Data
public class HotelDto {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String description;
    private Double rating;
}