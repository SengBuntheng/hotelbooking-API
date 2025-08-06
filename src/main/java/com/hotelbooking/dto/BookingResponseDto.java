package com.hotelbooking.dto;

import lombok.Data;

@Data
public class BookingResponseDto {
    private Long bookingId;
    private String paymentUrl;
}