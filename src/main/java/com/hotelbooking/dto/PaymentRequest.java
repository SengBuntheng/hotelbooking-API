package com.hotelbooking.dto;

import com.hotelbooking.Enum.PaymentMethod;
import com.hotelbooking.Enum.PaymentStatus;
import com.hotelbooking.model.Booking;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Getter
@Setter
public class PaymentRequest {


    private BigDecimal amount;

    private String paymentMethod;

    private String  paymentStatus;

    private String transactionId;

    private LocalDateTime paymentDate;

    private Booking bookingId;

    private String currency = "USD";

    private String qrCode;          // For QR code payments
    private String cardToken;       // For card payments
    private String cashReceiptNo;
}
