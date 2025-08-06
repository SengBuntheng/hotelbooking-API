package com.hotelbooking.service.handler;

import com.hotelbooking.Constant.Constant;
import com.hotelbooking.dto.AbaQrResponse;
import com.hotelbooking.dto.PaymentRequest;
import com.hotelbooking.service.ABAPayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;

@Service
@Slf4j
public class KhQRhandlerService {

    private final RestTemplate restTemplate;

    private final ABAPayService payService;
    public KhQRhandlerService(RestTemplate restTemplate, ABAPayService payService){
        this.restTemplate = restTemplate;
        this.payService = payService;
    }


    public AbaQrResponse PostingPaymentToPaymentGatePay(PaymentRequest paymentRequest) {
        log.info("PostingPaymentToPaymentGatePay");

        if (Constant.ABA_PAY.equalsIgnoreCase(paymentRequest.getPaymentMethod())) {
            log.info("Payment method is ABA_PAY");
            try {
                ResponseEntity<byte[]> qrResponse = payService.qrImage(
                        paymentRequest.getAmount().doubleValue(),
                        "USD",
                        paymentRequest.getTransactionId()
                );

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
                log.error("ABA Pay QR generation failed: {}", e.getMessage());
                return new AbaQrResponse(null, null, "Error: " + e.getMessage(), false);
            }
        }

        // handle other payment methods similarly, if needed
        return new AbaQrResponse(null, null, "Unsupported payment method", false);
    }
}