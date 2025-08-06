package com.hotelbooking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingCreationResponse {
    private Long bookingId;
    private String qrImageBase64; // The QR code will be sent as a Base64 string
}
