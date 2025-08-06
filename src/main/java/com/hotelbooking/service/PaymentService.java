package com.hotelbooking.service;

import com.hotelbooking.dto.PaymentRequest;
import org.springframework.stereotype.Service;

@Service
public interface PaymentService {
    String processPayment(PaymentRequest request);
    String checkPaymentStatus(String transactionId);
}
