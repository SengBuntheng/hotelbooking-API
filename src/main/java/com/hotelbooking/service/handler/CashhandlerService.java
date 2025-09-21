package com.hotelbooking.service.handler;

import com.hotelbooking.Constant.Constant;
import com.hotelbooking.dto.PaymentRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CashhandlerService {

    public CashhandlerService() {
        // No RestTemplate needed for local processing
    }

    /**
     * Simulates the processing of a cash payment by logging the transaction
     * and returning a success status.
     *
     * @param paymentRequest The details of the payment to be processed.
     * @return A success constant indicating the payment was processed.
     */
    public String processCashPayment(PaymentRequest paymentRequest) {
        log.info("Processing cash payment locally for transactionId: {}", paymentRequest.getTransactionId());
        // Here you could add logic to save the cash payment details to a local database or file if needed.
        log.info("Cash payment for amount {} successfully recorded for bookingId: {}",
                paymentRequest.getAmount(),
                paymentRequest.getBookingId().getId());

        return Constant.Success;
    }
}