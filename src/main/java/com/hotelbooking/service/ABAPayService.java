package com.hotelbooking.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotelbooking.Enum.BookingStatus;
import com.hotelbooking.Enum.PaymentStatusCode;
import com.hotelbooking.Repository.BookingRepository;
import com.hotelbooking.dto.*;
import com.hotelbooking.model.Booking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.SocketTimeoutException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;

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

    @Value("${aba.timeout.connect:30000}")
    private int connectTimeout;

    @Value("${aba.timeout.read:30000}")
    private int readTimeout;

    @Value("${aba.retry.max.attempts:3}")
    private int maxRetryAttempts;

    @Value("${aba.retry.delay:2000}")
    private int retryDelayMs;

    private final SimpMessagingTemplate messagingTemplate;
    private final BookingRepository bookingRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public ABAPayService(SimpMessagingTemplate messagingTemplate,
                         BookingRepository bookingRepository,
                         RestTemplate restTemplate) {
        this.messagingTemplate = messagingTemplate;
        this.bookingRepository = bookingRepository;
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();

        // Configure RestTemplate with timeouts if not already configured
        configureRestTemplateTimeouts();
    }

    private void configureRestTemplateTimeouts() {
        try {
            var factory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(connectTimeout);
            factory.setReadTimeout(readTimeout);
            ((org.springframework.web.client.RestTemplate) restTemplate).setRequestFactory(factory);
        } catch (Exception e) {
            logger.warn("Could not configure RestTemplate timeouts: {}", e.getMessage());
        }
    }

    public String getQrImageBase64(Booking booking) {
        if (booking == null) {
            throw new IllegalArgumentException("Booking cannot be null");
        }

        GenerateQrResponse qrResponse = proceedQrRequestWithRetry(booking);
        if (qrResponse == null || !"0".equals(qrResponse.getStatus().getCode())) {
            String errorMessage = qrResponse != null ? qrResponse.getStatus().getMessage() : "No response from ABA Pay";
            logger.error("Failed to generate QR code for booking {}: {}", booking.getId(), errorMessage);
            throw new RuntimeException("Failed to generate QR code data: " + errorMessage);
        }

        String qrImage = qrResponse.getQrImage();
        if (qrImage == null || !qrImage.contains(",")) {
            logger.error("Invalid qrImage format received from ABA Pay for booking {}", booking.getId());
            throw new RuntimeException("Invalid QR image data received.");
        }

        return qrImage.split(",")[1];
    }

    private GenerateQrResponse proceedQrRequestWithRetry(Booking booking) {
        int attempt = 0;
        Exception lastException = null;

        while (attempt < maxRetryAttempts) {
            try {
                attempt++;
                logger.info("Attempt {} to generate QR for booking {}", attempt, booking.getId());

                GenerateQrRequest requestBody = buildQrRequest(booking);
                requestBody.setHash(generateHashString(requestBody));

                return sendApiRequest("generate-qr", requestBody, GenerateQrResponse.class);

            } catch (ResourceAccessException e) {
                lastException = e;
                logger.warn("Network error on attempt {} for booking {}: {}", attempt, booking.getId(), e.getMessage());

                if (attempt < maxRetryAttempts) {
                    try {
                        Thread.sleep(retryDelayMs * attempt); // Exponential backoff
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("QR generation interrupted", ie);
                    }
                }
            } catch (Exception e) {
                // For non-network errors, don't retry
                logger.error("Non-retryable error generating QR for booking {}: {}", booking.getId(), e.getMessage());
                throw new RuntimeException("Failed to process QR request: " + e.getMessage(), e);
            }
        }

        logger.error("Failed to generate QR after {} attempts for booking {}", maxRetryAttempts, booking.getId());
        throw new RuntimeException("QR generation failed after " + maxRetryAttempts + " attempts due to network issues", lastException);
    }

    public ResponseEntity<byte[]> qrImage(Booking booking) {
        try {
            if (booking == null) {
                logger.error("Booking is null when generating QR image");
                return ResponseEntity.badRequest().build();
            }

            GenerateQrResponse exGenerateQrResponse = proceedQrRequestWithRetry(booking);
            if (exGenerateQrResponse == null || !exGenerateQrResponse.getStatus().getCode().equals("0")) {
                logger.error("Failed to generate QR for booking {}: {}",
                        booking.getId(),
                        exGenerateQrResponse != null ? exGenerateQrResponse.getStatus().getMessage() : "No response");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }

            String imgData = exGenerateQrResponse.getQrImage().split(",")[1];
            byte[] imageBytes = Base64.getDecoder().decode(imgData);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "image/png");
            headers.set("Cache-Control", "no-cache, no-store, must-revalidate");

            return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            logger.error("Error generating QR image for booking {}: {}",
                    booking != null ? booking.getId() : "null", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    public void txnCallback(CallbackRequest request) {
        if (request == null || request.getTran_id() == null) {
            logger.error("Invalid callback request received");
            return;
        }

        try {
            CheckTxnResponse checkTxnResponse = checkTransactionWithRetry(request.getTran_id());

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
                    updateBookingStatus(booking, checkTxnResponse.getData().getPayment_status_code());
                    bookingRepository.save(booking);
                } else {
                    logger.warn("Booking not found for tran_id={}", request.getTran_id());
                }
            }
        } catch (NumberFormatException e) {
            logger.error("Invalid transaction ID format: {}", request.getTran_id());
            sendPaymentStatus(request.getTran_id(), "FAIL, Invalid transaction ID");
        } catch (Exception e) {
            logger.error("Callback processing error for tran_id={}: {}", request.getTran_id(), e.getMessage(), e);
            sendPaymentStatus(request.getTran_id(), "FAIL, " + e.getMessage());
        }
    }

    private CheckTxnResponse checkTransactionWithRetry(String txnId) {
        int attempt = 0;
        Exception lastException = null;

        while (attempt < maxRetryAttempts) {
            try {
                attempt++;
                logger.info("Attempt {} to check transaction {}", attempt, txnId);

                CheckTxnRequest request = new CheckTxnRequest();
                String reqTime = dateTimeString();
                request.setReq_time(reqTime);
                request.setMerchant_id(merchantId);
                request.setTran_id(txnId);
                request.setHash(generateHashVerifyTxn(txnId, reqTime));

                return sendApiRequest("check-transaction-2", request, CheckTxnResponse.class);

            } catch (ResourceAccessException e) {
                lastException = e;
                logger.warn("Network error on attempt {} for transaction {}: {}", attempt, txnId, e.getMessage());

                if (attempt < maxRetryAttempts) {
                    try {
                        Thread.sleep(retryDelayMs * attempt);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Transaction check interrupted", ie);
                    }
                }
            } catch (Exception e) {
                // For non-network errors, don't retry
                logger.error("Non-retryable error checking transaction {}: {}", txnId, e.getMessage());
                throw e;
            }
        }

        logger.error("Failed to check transaction after {} attempts for {}", maxRetryAttempts, txnId);
        throw new RuntimeException("Transaction check failed after " + maxRetryAttempts + " attempts", lastException);
    }

    private void updateBookingStatus(Booking booking, int paymentStatusCode) {
        PaymentStatusCode status = PaymentStatusCode.fromCode(paymentStatusCode);

        switch (status) {
            case SUCCESS:
                booking.setBookingStatus(BookingStatus.CONFIRMED);
                sendPaymentStatus(String.valueOf(booking.getId()), "SUCCESS");
                logger.info("Booking {} confirmed successfully", booking.getId());
                break;
            case PENDING:
                booking.setBookingStatus(BookingStatus.IN_PROGRESS);
                sendPaymentStatus(String.valueOf(booking.getId()), "PENDING");
                logger.info("Booking {} is pending", booking.getId());
                break;
            default:
                booking.setBookingStatus(BookingStatus.CANCELLED);
                sendPaymentStatus(String.valueOf(booking.getId()), "FAIL");
                logger.warn("Booking {} failed with status code {}", booking.getId(), paymentStatusCode);
                break;
        }
    }

    private GenerateQrRequest buildQrRequest(Booking booking) {
        GenerateQrRequest requestBody = new GenerateQrRequest();
        requestBody.setAmount(booking.getTotalAmount().doubleValue());
        requestBody.setCurrency("USD");
        requestBody.setCallback_url(encodeCallBackUrl());
        requestBody.setLifetime(30); // 30 minutes
        requestBody.setMerchant_id(merchantId);
        requestBody.setPayment_option("abapay_khqr");
        requestBody.setQr_image_template("template6_color");
        requestBody.setReq_time(dateTimeString());
        requestBody.setTran_id(String.valueOf(booking.getId()));

        // Populate user details from the booking object
        if (booking.getUser() != null) {
            requestBody.setFirst_name(booking.getUser().getFirstName());
            requestBody.setLast_name(booking.getUser().getLastName());
            requestBody.setEmail(booking.getUser().getEmail());
            requestBody.setPhone(booking.getUser().getPhone());
        }

        // Initialize optional fields to ensure hash consistency
        requestBody.setItems("");
        requestBody.setCustom_fields("");
        requestBody.setPurchase_type("");
        requestBody.setReturn_deeplink("");
        requestBody.setReturn_params("");
        requestBody.setPayout("");

        return requestBody;
    }

    private <T> T sendApiRequest(String endpoint, Object request, Class<T> responseClass) {
        String fullUrl = baseUrl + endpoint;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("User-Agent", "HotelBooking-App/1.0");

            String jsonPayload = objectMapper.writeValueAsString(request);
            HttpEntity<String> entity = new HttpEntity<>(jsonPayload, headers);

            logger.info("Sending POST request to ABA PAY API URL: {}", fullUrl);
            logger.debug("Payload: {}", jsonPayload);

            ResponseEntity<String> response = restTemplate.postForEntity(fullUrl, entity, String.class);

            String responseBody = response.getBody();
            logger.info("Received response from ABA PAY API Status: {}", response.getStatusCode());

            if (response.getStatusCode().is2xxSuccessful() && responseBody != null && responseBody.trim().startsWith("{")) {
                return objectMapper.readValue(responseBody, responseClass);
            } else {
                logger.error("Received an invalid or non-JSON response from the payment gateway: {}", responseBody);
                throw new RuntimeException("Received an invalid response from the payment gateway.");
            }
        } catch (HttpClientErrorException e) {
            logger.error("ABA Pay API returned client error: {} {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Payment gateway client error: " + e.getStatusCode(), e);
        } catch (ResourceAccessException e) {
            logger.error("Network error connecting to ABA Pay API: {}", e.getMessage());
            throw new ResourceAccessException("Network connection error: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error during API request to {}: {}", fullUrl, e.getMessage());
            throw new RuntimeException("API request failed: " + e.getMessage(), e);
        }
    }

    // ... (keep the existing helper methods: generateHashString, generateHashVerifyTxn,
    // createHmacSha512, sendPaymentStatus, encodeCallBackUrl, dateTimeString)

    private String generateHashString(GenerateQrRequest request) {
        StringJoiner data = new StringJoiner("");
        data.add(request.getReq_time());
        data.add(request.getMerchant_id());
        data.add(request.getTran_id());
        data.add(String.valueOf(request.getAmount()));
        if (request.getItems() != null) data.add(request.getItems());
        if (request.getFirst_name() != null) data.add(request.getFirst_name());
        if (request.getLast_name() != null) data.add(request.getLast_name());
        if (request.getEmail() != null) data.add(request.getEmail());
        if (request.getPhone() != null) data.add(request.getPhone());
        if (request.getPurchase_type() != null) data.add(request.getPurchase_type());
        if (request.getPayment_option() != null) data.add(request.getPayment_option());
        if (request.getCallback_url() != null) data.add(request.getCallback_url());
        if (request.getReturn_deeplink() != null) data.add(request.getReturn_deeplink());
        if (request.getCurrency() != null) data.add(request.getCurrency());
        if (request.getCustom_fields() != null) data.add(request.getCustom_fields());
        if (request.getReturn_params() != null) data.add(request.getReturn_params());
        if (request.getPayout() != null) data.add(request.getPayout());
        if (request.getLifetime() > 0) data.add(String.valueOf(request.getLifetime()));
        if (request.getQr_image_template() != null) data.add(request.getQr_image_template());
        return createHmacSha512(data.toString());
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
            throw new RuntimeException("Hash generation failed", e);
        }
    }

    private void sendPaymentStatus(String transactionId, String status) {
        logger.info("Sending payment status update for transactionId='{}': {}", transactionId, status);
        try {
            messagingTemplate.convertAndSend("/topic/payment-status", Map.of(
                    "transactionId", transactionId,
                    "status", status,
                    "timestamp", LocalDateTime.now().toString()
            ));
        } catch (Exception e) {
            logger.error("Failed to send payment status update: {}", e.getMessage(), e);
        }
    }

    private String encodeCallBackUrl() {
        return Base64.getEncoder().encodeToString(callbackUrl.getBytes());
    }

    private String dateTimeString() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }
}