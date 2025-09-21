package com.hotelbooking.service.handler;

import com.hotelbooking.Constant.Constant;
import com.hotelbooking.Repository.PaymentRepository;
import com.hotelbooking.dto.PaymentRequest;
import com.hotelbooking.model.Payment;
import com.hotelbooking.service.ABAPayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.hotelbooking.Enum.PaymentStatus;


import java.time.LocalDateTime;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@Service
public class PaymentHandlerService {

    private final Map<String, Function<PaymentRequest, String>> paymentHandlers;
    private final PaymentRepository paymentRepository;


    public PaymentHandlerService(KhQRhandlerService khQRhandlerService,
                                 CashhandlerService cashhandlerService,
                                 ABAPayService abaPayService,
                                 PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
        this.paymentHandlers = Map.of(
                Constant.BANK, request -> handleBankPayment(khQRhandlerService, request),
                Constant.CASH, cashhandlerService::processCashPayment,
                Constant.ABA_PAY, request -> handleAbaPayPayment(abaPayService, khQRhandlerService, request),
                Constant.CARD, this::handleCardPayment
        );
    }

    public String handlePayment(PaymentRequest paymentRequest) {
        String paymentMethod = paymentRequest.getPaymentMethod();
        Function<PaymentRequest, String> handler = paymentHandlers.get(paymentMethod.toUpperCase());

        if (handler != null) {
            log.info("Processing payment with method: {}", paymentMethod);
            String result = handler.apply(paymentRequest);
            savePayment(paymentRequest, result);
            return result;
        } else {
            log.warn("Payment method not supported: {}", paymentMethod);
            return Constant.Failure;
        }
    }

    private String handleBankPayment(KhQRhandlerService khQRhandlerService, PaymentRequest request) {
        return String.valueOf(khQRhandlerService.generateQrCode(request));
    }


    private String handleAbaPayPayment(ABAPayService abaPayService, KhQRhandlerService khQRhandlerService, PaymentRequest request) {
        try {
            var response = khQRhandlerService.generateQrCode(request);
            return response.isSuccess() ? Constant.Success : Constant.Failure;
        } catch (Exception e) {
            log.error("ABA Pay QR generation failed: {}", e.getMessage());
            return Constant.Failure;
        }
    }


    private String handleCardPayment(PaymentRequest request) {
        log.info("Processing card payment for transactionId: {}", request.getTransactionId());
        // Placeholder for card payment logic
        return Constant.Success;
    }

    public void savePayment(PaymentRequest request, String qrData) {
        Payment payment = Payment.builder()
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod())
                .paymentStatus(PaymentStatus.PENDING)
                .transactionId(request.getTransactionId())
                .qrCodeData(qrData)
                .currency("USD")
                .user(request.getBookingId().getUser())
                .booking(request.getBookingId())
                .paymentDate(LocalDateTime.now())
                .createdBy(Constant.SYSTEM)
                .updatedBy(Constant.SYSTEM)
                .build();
        paymentRepository.save(payment);
        log.info("Payment saved for transactionId: {}", request.getTransactionId());
    }
}