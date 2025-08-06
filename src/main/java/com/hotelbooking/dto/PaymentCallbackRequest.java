package com.hotelbooking.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentCallbackRequest {
    private String transactionId;
    private String status;
    private String referenceNumber;
    private BigDecimal amount;
    private String currency;
}