package com.hotelbooking.Controller;

import com.hotelbooking.Repository.BookingRepository;
import com.hotelbooking.dto.CallbackRequest;
import com.hotelbooking.dto.ApiResponse;
import com.hotelbooking.model.Booking;
import com.hotelbooking.service.ABAPayService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/v1/payments/aba")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class PaymentController {

    private final ABAPayService abaPayService;
    private final BookingRepository bookingRepository;

    @GetMapping("/generate-qr-image")
    public ResponseEntity<byte[]> generateQrImage(@RequestParam String txnId) {
        try {
            if (txnId == null || txnId.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            Long bookingId = Long.parseLong(txnId);
            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Booking not found with id: " + txnId));

            log.info("Generating QR image for booking ID: {}", bookingId);
            return abaPayService.qrImage(booking);

        } catch (NumberFormatException e) {
            log.error("Invalid transaction ID format: {}", txnId);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Failed to generate QR image for txnId {}: {}", txnId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/qr-base64/{bookingId}")
    public ResponseEntity<ApiResponse<Map<String, String>>> getQrBase64(@PathVariable Long bookingId) {
        try {
            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingId));

            String qrBase64 = abaPayService.getQrImageBase64(booking);

            Map<String, String> response = new HashMap<>();
            response.put("qrImage", qrBase64);
            response.put("bookingId", bookingId.toString());

            return ResponseEntity.ok(ApiResponse.success("QR code generated successfully", response));

        } catch (Exception e) {
            log.error("Failed to generate QR base64 for booking {}: {}", bookingId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to generate QR code"));
        }
    }

    @PostMapping("/callback")
    public ResponseEntity<ApiResponse<String>> handleCallback(@Valid @RequestBody CallbackRequest request) {
        try {
            log.info("Received ABA payment callback for transaction: {}", request.getTran_id());
            abaPayService.txnCallback(request);
            return ResponseEntity.ok(ApiResponse.success("Callback processed successfully", null));
        } catch (Exception e) {
            log.error("Failed to process callback for transaction {}: {}", request.getTran_id(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to process callback"));
        }
    }

    @GetMapping("/payment-status/{bookingId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPaymentStatus(@PathVariable Long bookingId) {
        try {
            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingId));

            Map<String, Object> status = new HashMap<>();
            status.put("bookingId", bookingId);
            status.put("status", booking.getBookingStatus().toString());
            status.put("totalAmount", booking.getTotalAmount());

            return ResponseEntity.ok(ApiResponse.success("Payment status retrieved", status));

        } catch (Exception e) {
            log.error("Failed to get payment status for booking {}: {}", bookingId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Booking not found"));
        }
    }
}