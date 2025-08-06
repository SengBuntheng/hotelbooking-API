package com.hotelbooking.service.handler;

import com.hotelbooking.Constant.Constant;
import com.hotelbooking.Enum.PaymentStatus;
import com.hotelbooking.Repository.PaymentRepository;
import com.hotelbooking.dto.AbaQrResponse;
import com.hotelbooking.dto.PaymentRequest;
import com.hotelbooking.model.Payment;
import com.hotelbooking.service.ABAPayService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Slf4j
@Service
public class PaymentHandlerService {



    private final KhQRhandlerService khQRhandlerService;
    private final  CashhandlerService cashhandlerService;
    private final ABAPayService abaPayService;  // Add this
    private final PaymentRepository paymentRepository;

    public PaymentHandlerService(KhQRhandlerService khQRhandlerService,
                                 CashhandlerService cashhandlerService, KhQRhandlerService khQRhandlerService1, CashhandlerService cashhandlerService1,
                                 ABAPayService abaPayService,
                                 PaymentRepository paymentRepository) {
        this.khQRhandlerService = khQRhandlerService1;
        this.cashhandlerService = cashhandlerService1;

        this.abaPayService = abaPayService;
        this.paymentRepository = paymentRepository;
    }


    public String PostingPaymentToPaymentGatePay(PaymentRequest paymentRequest) {
        log.info("PostingPaymentToPaymentGatePay");

        if(Constant.BANK.equalsIgnoreCase(paymentRequest.getPaymentMethod())){
            log.info("Payment method is BANK");
            String khqrServerResponse = String.valueOf(khQRhandlerService.PostingPaymentToPaymentGatePay(paymentRequest));
            savePayment(paymentRequest, khqrServerResponse);
            return StringUtils.hasText(khqrServerResponse) ? Constant.Success : Constant.Failure;
        }
        else if (Constant.CASH.equalsIgnoreCase(paymentRequest.getPaymentMethod())){
            log.info("Payment method is CASH");
            String cashServerResponse = cashhandlerService.processCashPayment(paymentRequest);
            savePayment(paymentRequest, cashServerResponse);
            return StringUtils.hasText(cashServerResponse) ? Constant.Success : Constant.Failure;
        }
        else if(Constant.ABA_PAY.equalsIgnoreCase(paymentRequest.getPaymentMethod())){
            log.info("Payment method is ABA_PAY");
            try {
                // Generate QR code for ABA Pay
                ResponseEntity<byte[]> qrResponse = abaPayService.qrImage(
                        paymentRequest.getAmount().doubleValue(),
                        "USD",  // Assuming USD currency
                        paymentRequest.getTransactionId()
                );
                AbaQrResponse response = khQRhandlerService.PostingPaymentToPaymentGatePay(paymentRequest);
                if (response.isSuccess()) {
                    savePayment(paymentRequest, "ABA_PAY_QR_GENERATED");
                    return Constant.Success;
                }
                return Constant.Failure;
            } catch (Exception e) {
                log.error("ABA Pay QR generation failed: {}", e.getMessage());
                return Constant.Failure;
            }
        }
        else if(Constant.CARD.equalsIgnoreCase(paymentRequest.getPaymentMethod())){
            log.info("Payment method is CARD");
            return Constant.Success;
        }

        log.info("Payment method not supported");
        return Constant.Failure;
    }
    public void savePayment(PaymentRequest request, String qrBase64) {
        Payment payment = Payment.builder()
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod())
                .paymentStatus(PaymentStatus.PENDING)
                .transactionId(request.getTransactionId())
                .qrCodeData(qrBase64)
                .currency("USD")
                .user(request.getBookingId().getUser())
                .booking(request.getBookingId())
                .paymentDate(LocalDateTime.now())
                .createdBy("SYSTEM")
                .updatedBy("SYSTEM")
                .build();

        paymentRepository.save(payment);
    }

}