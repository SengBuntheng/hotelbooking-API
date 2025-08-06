package com.hotelbooking.service.impl;

import com.hotelbooking.dto.PaymentRequest;
import com.hotelbooking.service.PaymentService;
import com.hotelbooking.service.handler.PaymentHandlerService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentHandlerService paymentHandlerService;

    public PaymentServiceImpl( PaymentHandlerService paymentHandlerService) {
        this.paymentHandlerService = paymentHandlerService;
    }

    @Override
    public String processPayment(PaymentRequest request) {
        log.info("Processing payment through handler service");
        return paymentHandlerService.PostingPaymentToPaymentGatePay(request);
    }

    @Override
    public String checkPaymentStatus(String transactionId) {
        // You may replace this with real DB check logic.
        log.info("Checking payment status for txnId: {}", transactionId);
        // For now, return hardcoded or mock data.
        return "PENDING";
    }
}
