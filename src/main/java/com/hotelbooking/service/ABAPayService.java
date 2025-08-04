package com.hotelbooking.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotelbooking.dto.*;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Map;

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

    public ABAPayService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public ResponseEntity<byte[]> qrImage(double amount, String ccy, String txnId) {
        try {
            GenerateQrResponse exGenerateQrResponse = proceedQrRequest(amount, ccy, txnId);
            if (exGenerateQrResponse == null || !exGenerateQrResponse.getStatus().getCode().equals("0")) {
                logger.error("Failed to generate QR: {}",
                        exGenerateQrResponse != null ? exGenerateQrResponse.getStatus().getMessage() : "No response");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }

            String imgData = exGenerateQrResponse.getQrImage().split(",")[1];
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

            if (checkTxnResponse == null || !checkTxnResponse.getStatus().getCode().equals("00")) {
                String message = checkTxnResponse != null ? checkTxnResponse.getStatus().getMessage() : "No response";
                sendPaymentStatus(request.getTran_id(), "FAIL, " + message);
                return;
            }

            if (checkTxnResponse.getData() != null) {
                switch (checkTxnResponse.getData().getPayment_status_code()) {
                    case 0 -> sendPaymentStatus(request.getTran_id(), "SUCCESS");
                    case 2 -> sendPaymentStatus(request.getTran_id(), "PENDING");
                    case 3 -> sendPaymentStatus(request.getTran_id(), "DECLINED");
                    case 4 -> sendPaymentStatus(request.getTran_id(), "REFUNDED");
                    case 7 -> sendPaymentStatus(request.getTran_id(), "CANCELLED");
                    default -> sendPaymentStatus(request.getTran_id(), "FAILED");
                }
            }
        } catch (Exception e) {
            logger.error("Callback processing error: {}", e.getMessage(), e);
            sendPaymentStatus(request.getTran_id(), "FAIL, " + e.getMessage());
        }
    }

    // ******************* Internal Helpers ********************

    private GenerateQrResponse proceedQrRequest(double amount, String ccy, String txnId) {
        try {
            GenerateQrRequest requestBody = new GenerateQrRequest();
            requestBody.setAmount(amount);
            requestBody.setCurrency(ccy);
            requestBody.setCallback_url(encodeCallBackUrl());
            requestBody.setLifetime(3);
            requestBody.setMerchant_id(merchantId);
            requestBody.setPayment_option("abapay_khqr");
            requestBody.setQr_image_template("template6_color");
            requestBody.setReq_time(dateTimeString());
            requestBody.setTran_id(txnId);

            String hash = generateHashString(requestBody);
            requestBody.setHash(hash);

            return requestQr(requestBody);
        } catch (Exception e) {
            logger.error("Error processing QR request: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private GenerateQrResponse requestQr(GenerateQrRequest request) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(request);
            HttpResponse<String> response = Unirest.post(baseUrl + "generate-qr")
                    .header("Content-Type", "application/json")
                    .body(json)
                    .asString();

            return objectMapper.readValue(response.getBody(), GenerateQrResponse.class);
        } catch (Exception e) {
            logger.error("Error requesting QR: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private CheckTxnResponse checkTransaction(String txnId) {
        try {
            CheckTxnRequest request = new CheckTxnRequest();
            String reqTime = dateTimeString();
            request.setReq_time(reqTime);
            request.setMerchant_id(merchantId);
            request.setTran_id(txnId);
            request.setHash(generateHashVerifyTxn(txnId, reqTime));

            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(request);

            HttpResponse<String> response = Unirest.post(baseUrl + "check-transaction-2")
                    .header("Content-Type", "application/json")
                    .body(json)
                    .asString();

            return objectMapper.readValue(response.getBody(), CheckTxnResponse.class);
        } catch (Exception e) {
            logger.error("Transaction check error: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private String generateHashString(GenerateQrRequest request) {
        try {
            String b4hash = summaryObjectToString(request);
            Mac sha512HMAC = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(apiKey.getBytes(), "HmacSHA512");
            sha512HMAC.init(secretKey);
            byte[] hashBytes = sha512HMAC.doFinal(b4hash.getBytes());
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (Exception e) {
            logger.error("Hash generation failed: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private String generateHashVerifyTxn(String txnId, String reqTime) {
        try {
            String plainHash = reqTime + merchantId + txnId;
            Mac sha512HMAC = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(apiKey.getBytes(), "HmacSHA512");
            sha512HMAC.init(secretKey);
            byte[] hashBytes = sha512HMAC.doFinal(plainHash.getBytes());
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (Exception e) {
            logger.error("Verification hash generation failed: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private String summaryObjectToString(GenerateQrRequest request) {
        return (request.getReq_time() + request.getMerchant_id() +
                request.getTran_id() + request.getAmount() +
                request.getItems() + request.getFirst_name() +
                request.getLast_name() + request.getEmail() +

                request.getPhone() + request.getPurchase_type() +
                request.getPayment_option() + request.getCallback_url() +
                request.getReturn_deeplink() + request.getCurrency() +
                request.getCustom_fields() + request.getReturn_params() +
                request.getPayout() + request.getLifetime() +
                request.getQr_image_template()).replaceAll("null", "");
    }

    private void sendPaymentStatus(String transactionId, String status) {
        messagingTemplate.convertAndSend("/topic/payment-status", Map.of(
                "transactionId", transactionId,
                "status", status
        ));
    }

    private String encodeCallBackUrl() {
        return Base64.getEncoder().encodeToString(callbackUrl.getBytes());
    }

    private String dateTimeString() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        return now.format(formatter);
    }
}
