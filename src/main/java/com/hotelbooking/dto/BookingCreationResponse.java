package com.hotelbooking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingCreationResponse {
    private Long bookingId;
    private String qrCodeBase64;
    private String totalAmount;
    private String bookingStatus;
    private String message;

    public BookingCreationResponse(Long bookingId, String qrCodeBase64, String totalAmount, String bookingStatus) {
        this.bookingId = bookingId;
        this.qrCodeBase64 = qrCodeBase64;
        this.totalAmount = totalAmount;
        this.bookingStatus = bookingStatus;
        this.message = "Booking created successfully. Please scan the QR code to complete payment.";
    }

    public BookingCreationResponse(long id, String qrCodeBase64) {
        this.bookingId = id;
        this.qrCodeBase64 = qrCodeBase64;
        this.message = "Booking created successfully. Please scan the QR code to complete payment.";
    }
}