package com.hotelbooking.service.impl;

import com.hotelbooking.Repository.PaymentRepository;
import com.hotelbooking.dto.PaymentRequest;
import com.hotelbooking.model.Payment;
import com.hotelbooking.service.PaymentService;
import com.hotelbooking.service.handler.PaymentHandlerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentHandlerService paymentHandlerService;
    private final PaymentRepository paymentRepository;


    public PaymentServiceImpl(PaymentHandlerService paymentHandlerService, PaymentRepository paymentRepository) {
        this.paymentHandlerService = paymentHandlerService;
        this.paymentRepository = paymentRepository;
    }

    @Override
    public String processPayment(PaymentRequest request) {
        log.info("Processing payment for transactionId: {}", request.getTransactionId());
        return paymentHandlerService.handlePayment(request);
    }

    @Override
    public String checkPaymentStatus(String transactionId) {
        log.info("Checking payment status for transactionId: {}", transactionId);
        Optional<Payment> paymentOptional = paymentRepository.findByTransactionId(transactionId);

        return paymentOptional
                .map(payment -> payment.getPaymentStatus().name())
                .orElse("NOT_FOUND");
    }
}