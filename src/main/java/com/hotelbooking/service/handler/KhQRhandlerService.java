package com.hotelbooking.service.handler;

import com.hotelbooking.Constant.Constant;
import com.hotelbooking.dto.AbaQrResponse;
import com.hotelbooking.dto.PaymentRequest;
import com.hotelbooking.service.ABAPayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Service
@Slf4j
public class KhQRhandlerService {

    private final ABAPayService payService;

    public KhQRhandlerService(ABAPayService payService) {
        this.payService = payService;
    }

    public AbaQrResponse generateQrCode(PaymentRequest paymentRequest) {
        log.info("Generating QR code for transactionId: {}", paymentRequest.getTransactionId());

        if (!Constant.ABA_PAY.equalsIgnoreCase(paymentRequest.getPaymentMethod())) {
            return new AbaQrResponse(null, null, "Unsupported payment method", false);
        }

        try {
            // Corrected: Pass the entire booking object to the qrImage method
            ResponseEntity<byte[]> qrResponse = payService.qrImage(paymentRequest.getBookingId());

            if (qrResponse.getStatusCode().is2xxSuccessful()) {
                String base64Qr = Base64.getEncoder().encodeToString(qrResponse.getBody());
                return new AbaQrResponse(
                        paymentRequest.getTransactionId(),
                        base64Qr,
                        "QR generated successfully",
                        true
                );
            }

            return new AbaQrResponse(null, null, "Failed to generate QR", false);

        } catch (Exception e) {
            log.error("ABA Pay QR generation failed for transactionId: {}: {}", paymentRequest.getTransactionId(), e.getMessage());
            return new AbaQrResponse(null, null, "Error: " + e.getMessage(), false);
        }
    }
}