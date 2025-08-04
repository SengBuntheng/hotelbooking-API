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

    // *********************************************************************************** //
    // *********************************************************************************** //

    public ResponseEntity<byte[]> qrImage(double amount, String ccy, String txnId) {
        try {
            GenerateQrResponse exGenerateQrResponse = proceedQrRequest(amount, ccy, txnId);
            if (exGenerateQrResponse == null) return null;

            // Check status before assign
            if (!exGenerateQrResponse.getStatus().getCode().equals("0")) {
                logger.error("Error request generate ABA KHQR: {}", exGenerateQrResponse.getStatus().getMessage());
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }

            String base64Img = exGenerateQrResponse.getQrImage();
            String imgData = base64Img.split(",")[1];
            byte[] imageBytes = Base64.getDecoder().decode(imgData);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "image/png");

            return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error generating QR image: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public void txnCallback(CallbackRequest request) {
        try {

            // Uncomment this if you want websocket broadcast always success
            // sendPaymentStatus(request.getTran_id(), "SUCCESS");

            CheckTxnResponse checkTxnResponse = checkTransaction(request.getTran_id());

            if (checkTxnResponse == null || !checkTxnResponse.getStatus().getCode().equals("00")) {
                assert checkTxnResponse != null;
                sendPaymentStatus(request.getTran_id(), "FAIL, " + checkTxnResponse.getStatus().getMessage());
                return;
            }

            if (checkTxnResponse.getData() != null) {

                switch (checkTxnResponse.getData().getPayment_status_code()) {
                    case 0:
                        sendPaymentStatus(request.getTran_id(), "SUCCESS");
                        break;
                    case 2:
                        sendPaymentStatus(request.getTran_id(), "PENDING");
                        break;
                    case 3:
                        sendPaymentStatus(request.getTran_id(), "DECLINED");
                        break;
                    case 4:
                        sendPaymentStatus(request.getTran_id(), "REFUNDED");
                        break;
                    case 7:
                        sendPaymentStatus(request.getTran_id(), "CANCELLED");
                        break;
                    default:
                        sendPaymentStatus(request.getTran_id(), "FAILED");
                        break;
                }

            }

        } catch (Exception e) {
            logger.error("Error processing transaction callback: {}", e.getMessage(), e);
            sendPaymentStatus(request.getTran_id(), "FAIL, " + e.getMessage());
        }
    }

    // *********************************************************************************** //
    // *********************************************************************************** //

    private String generateHashString(GenerateQrRequest request) {
        String b4hash = summaryObjectToString(request);

        try {
            // Generate the HMAC hash using SHA-512
            Mac sha512HMAC = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKeySpec = new SecretKeySpec(apiKey.getBytes(), "HmacSHA512");
            sha512HMAC.init(secretKeySpec);
            byte[] hashBytes = sha512HMAC.doFinal(b4hash.getBytes());
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (Exception e) {
            logger.error("Error generating hash string: {}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
    }

    private static String summaryObjectToString(GenerateQrRequest request) {
        return (request.getReq_time() + request.getMerchant_id() +
                request.getTran_id() + request.getAmount() +
                request.getItems() + request.getFirst_name() +
                request.getLast_name() + request.getEmail() +
                request.getPhone() + request.getPurchase_type() +
                request.getPayment_option() + request.getCallback_url() +
                request.getReturn_deeplink() + request.getCurrency() +
                request.getCustom_fields() + request.getReturn_params() +
                request.getPayout() + request.getLifetime() +
                request.getQr_image_template()
        ).replaceAll("null", "");
    }

    private GenerateQrResponse requestQr(GenerateQrRequest request) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(request);
            Unirest.setTimeouts(0, 0);
            HttpResponse<String> response = Unirest.post(baseUrl + "generate-qr")
                    .header("Content-Type", "application/json")
                    .body(json)
                    .asString();
            return objectMapper.readValue(response.getBody(), GenerateQrResponse.class);
        } catch (Exception e) {
            logger.error("Error requesting QR: {}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
    }

    private GenerateQrResponse proceedQrRequest(double amount, String ccy, String txnId) {
        try {
            GenerateQrRequest requestBody = new GenerateQrRequest();
            requestBody.setAmount(amount);
            requestBody.setCurrency(ccy);
            requestBody.setCallback_url(encodeCallBackUrl());
            requestBody.setLifetime(3);
            requestBody.setMerchant_id(merchantId);
            requestBody.setPayment_option("abapay_khqr");
            requestBody.setQr_image_template("template6_color"); // Reference: https://developer.payway.com.kh/folder-3158158#customize-khqr-display-optional
            requestBody.setReq_time(dateTimeString());
            requestBody.setTran_id(txnId);

            String hash = generateHashString(requestBody);
            requestBody.setHash(hash);

            return requestQr(requestBody);
        } catch (Exception e) {
            logger.error("Error processing QR request: {}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
    }

    private void sendPaymentStatus(String transactionId, String status) {
        messagingTemplate.convertAndSend("/topic/payment-status", Map.of(
                "transactionId", transactionId,
                "status", status
        ));
    }

    private CheckTxnResponse checkTransaction(String txnId) {
        try {
            CheckTxnRequest request = new CheckTxnRequest();
            request.setReq_time(dateTimeString());
            request.setMerchant_id(merchantId);
            request.setTran_id(txnId);
            request.setHash(generateHashVerifyTxn(txnId, request.getReq_time()));

            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(request);

            Unirest.setTimeouts(0, 0);
            HttpResponse<String> response = Unirest.post(baseUrl + "check-transaction-2")
                    .header("Content-Type", "application/json")
                    .body(json)
                    .asString();

            return objectMapper.readValue(response.getBody(), CheckTxnResponse.class);
        } catch (Exception e) {
            logger.error("Error checking transaction: {}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
    }

    private String generateHashVerifyTxn(String txnId, String reqTime) {
        String plainHash = reqTime + merchantId + txnId;
        try {
            // Generate the HMAC hash using SHA-512
            Mac sha512HMAC = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKeySpec = new SecretKeySpec(apiKey.getBytes(), "HmacSHA512");
            sha512HMAC.init(secretKeySpec);
            byte[] hashBytes = sha512HMAC.doFinal(plainHash.getBytes());
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (Exception e) {
            logger.error("Error generating hash for transaction verification: {}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
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