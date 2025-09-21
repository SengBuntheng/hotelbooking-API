package com.hotelbooking.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotelbooking.Repository.BookingRepository;
import com.hotelbooking.dto.*;
import com.hotelbooking.model.Booking;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

@Service
public class ABAPayService {

    private static final Logger logger = LoggerFactory.getLogger(ABAPayService.class);

    @Value("${aba.public.key}")
    private String apiKey;

    @Value("${aba.api}")
    private String baseUrl;

    @Value("${aba.merchant}")
    private String merchantId;

    @Value("${aba.callback}")
    private String callbackUrl;

    private final SimpMessagingTemplate messagingTemplate;
    private final BookingRepository bookingRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ABAPayService(SimpMessagingTemplate messagingTemplate, BookingRepository bookingRepository) {
        this.messagingTemplate = messagingTemplate;
        this.bookingRepository = bookingRepository;
    }

    public String getQrImageBase64(Booking booking) {
        GenerateQrResponse qrResponse = proceedQrRequest(booking);
        if (qrResponse == null || !"0".equals(qrResponse.getStatus().getCode())) {
            String errorMessage = qrResponse != null ? qrResponse.getStatus().getMessage() : "No response from ABA Pay";
            logger.error("Failed to generate QR code: {}", errorMessage);
            throw new RuntimeException("Failed to generate QR code data: " + errorMessage);
        }

        String qrImage = qrResponse.getQrImage();
        if (qrImage == null || !qrImage.contains(",")) {
            logger.error("Invalid qrImage format received from ABA Pay.");
            throw new RuntimeException("Invalid QR image data received.");
        }
        return qrImage.split(",")[1];
    }

    public ResponseEntity<byte[]> qrImage(Booking booking) {
        try {
            GenerateQrResponse response = proceedQrRequest(booking);
            if (response == null || !"0".equals(response.getStatus().getCode())) {
                logger.error("Failed to generate QR: {}", response != null ? response.getStatus().getMessage() : "No response");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }

            String imgData = response.getQrImage().split(",")[1];
            byte[] imageBytes = Base64.getDecoder().decode(imgData);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "image/png");

            return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error generating QR image: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    public void txnCallback(CallbackRequest request) {
        try {
            CheckTxnResponse checkTxnResponse = checkTransaction(request.getTran_id());

            if (checkTxnResponse == null || !"00".equals(checkTxnResponse.getStatus().getCode())) {
                String message = checkTxnResponse != null ? checkTxnResponse.getStatus().getMessage() : "No response";
                logger.error("Transaction check failed for tran_id={}: {}", request.getTran_id(), message);
                sendPaymentStatus(request.getTran_id(), "FAIL, " + message);
                return;
            }

            if (checkTxnResponse.getData() != null) {
                Optional<Booking> optionalBooking = bookingRepository.findById(Long.parseLong(request.getTran_id()));
                if (optionalBooking.isPresent()) {
                    Booking booking = optionalBooking.get();
                    switch (checkTxnResponse.getData().getPayment_status_code()) {
                        case 0:
                            booking.setBookingStatus(com.hotelbooking.Enum.BookingStatus.CONFIRMED);
                            sendPaymentStatus(request.getTran_id(), "SUCCESS");
                            break;
                        case 2:
                            booking.setBookingStatus(com.hotelbooking.Enum.BookingStatus.IN_PROGRESS);
                            sendPaymentStatus(request.getTran_id(), "PENDING");
                            break;
                        default:
                            booking.setBookingStatus(com.hotelbooking.Enum.BookingStatus.CANCELLED);
                            sendPaymentStatus(request.getTran_id(), "FAILED");
                            break;
                    }
                    bookingRepository.save(booking);
                } else {
                    logger.warn("Booking not found for tran_id={}", request.getTran_id());
                }
            }
        } catch (Exception e) {
            logger.error("Callback processing error for tran_id={}: {}", request.getTran_id(), e.getMessage(), e);
            sendPaymentStatus(request.getTran_id(), "FAIL, " + e.getMessage());
        }
    }

    private GenerateQrResponse proceedQrRequest(Booking booking) {
        GenerateQrRequest requestBody = new GenerateQrRequest();
        requestBody.setAmount(booking.getTotalAmount().doubleValue());
        requestBody.setCurrency("USD");
        requestBody.setCallback_url(callbackUrl);
        requestBody.setLifetime(3);
        requestBody.setMerchant_id(merchantId);
        requestBody.setPayment_option("abapay_khqr");
        requestBody.setQr_image_template("template6_color");
        requestBody.setReq_time(dateTimeString());
        requestBody.setTran_id(String.valueOf(booking.getId()));

        if (booking.getUser() != null) {
            requestBody.setFirst_name(booking.getUser().getFirstName());
            requestBody.setLast_name(booking.getUser().getLastName());
            requestBody.setEmail(booking.getUser().getEmail());
            requestBody.setPhone(booking.getUser().getPhone());
        }

        String hash = generateHashString(requestBody);
        requestBody.setHash(hash);

        return sendApiRequest("generate-qr", requestBody, GenerateQrResponse.class);
    }

    private CheckTxnResponse checkTransaction(String txnId) {
        CheckTxnRequest request = new CheckTxnRequest();
        String reqTime = dateTimeString();
        request.setReq_time(reqTime);
        request.setMerchant_id(merchantId);
        request.setTran_id(txnId);
        request.setHash(generateHashVerifyTxn(txnId, reqTime));

        return sendApiRequest("check-transaction-2", request, CheckTxnResponse.class);
    }

    private <T> T sendApiRequest(String endpoint, Object request, Class<T> responseClass) {
        String fullUrl = baseUrl + endpoint;
        try {
            String jsonPayload = objectMapper.writeValueAsString(request);

            logger.info("Sending POST request to ABA PAY API:");
            logger.info("URL: {}", fullUrl);
            logger.info("Payload: {}", jsonPayload);

            HttpResponse<String> response = Unirest.post(fullUrl)
                    .header("Content-Type", "application/json")
                    .body(jsonPayload)
                    .asString();

            String responseBody = response.getBody();
            logger.info("Received response from ABA PAY API:");
            logger.info("Status: {}", response.getStatus());
            logger.info("Body: {}", responseBody);

            if (response.getStatus() == 200 && responseBody != null && responseBody.trim().startsWith("{")) {
                return objectMapper.readValue(responseBody, responseClass);
            } else if (response.getStatus() == 500 && responseBody != null && responseBody.trim().startsWith("{")) {
                logger.error("ABA Pay API returned a 500 error: {}", responseBody);
                throw new RuntimeException("Received a server error from the payment gateway.");
            } else {
                logger.error("Received an invalid or non-JSON response from the payment gateway.");
                throw new RuntimeException("Received an invalid response from the payment gateway.");
            }
        } catch (Exception e) {
            logger.error("Error during API request to {}: {}", fullUrl, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    // ? Fixed: Generates hash using ALL fields in proper order
    private String generateHashString(GenerateQrRequest request) {
        String data = request.getReq_time() +
                request.getMerchant_id() +
                request.getTran_id() +
                request.getAmount() +
                safe(request.getItems()) +
                safe(request.getFirst_name()) +
                safe(request.getLast_name()) +
                safe(request.getEmail()) +
                safe(request.getPhone()) +
                safe(request.getPurchase_type()) +
                safe(request.getPayment_option()) +
                safe(request.getCallback_url()) +
                safe(request.getReturn_deeplink()) +
                safe(request.getCurrency()) +
                safe(request.getCustom_fields()) +
                safe(request.getReturn_params()) +
                safe(request.getPayout()) +
                request.getLifetime() +
                safe(request.getQr_image_template());

        return createHmacSha512(data);
    }

    private String generateHashVerifyTxn(String txnId, String reqTime) {
        return createHmacSha512(reqTime + merchantId + txnId);
    }

    private String createHmacSha512(String data) {
        try {
            Mac sha512HMAC = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(apiKey.getBytes(), "HmacSHA512");
            sha512HMAC.init(secretKey);
            byte[] hashBytes = sha512HMAC.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (Exception e) {
            logger.error("Hash generation failed: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private String safe(String value) {
        return value != null ? value : "";
    }

    private String dateTimeString() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    private void sendPaymentStatus(String transactionId, String status) {
        logger.info("Sending payment status update for transactionId='{}': {}", transactionId, status);
        messagingTemplate.convertAndSend("/topic/payment-status", Map.of(
                "transactionId", transactionId,
                "status", status
        ));
    }
}
