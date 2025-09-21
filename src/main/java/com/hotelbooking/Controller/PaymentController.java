package com.hotelbooking.Controller;

import com.hotelbooking.Repository.BookingRepository;
import com.hotelbooking.dto.CallbackRequest;
import com.hotelbooking.model.Booking;
import com.hotelbooking.service.ABAPayService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("v1/aba/")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PaymentController {

    private final ABAPayService abaPayService;
    private final BookingRepository bookingRepository; // Inject BookingRepository

    /**
     * Generates a QR code image for a given transaction (booking) ID.
     * This endpoint is useful for re-generating a QR code if the user needs it again.
     */
    @GetMapping("generate-qr-image")
    public ResponseEntity<byte[]> generateQrImage(@RequestParam String txnId) {
        // Find the booking by its ID (which is the transaction ID)
        Booking booking = bookingRepository.findById(Long.parseLong(txnId))
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + txnId));

        // Call the updated qrImage method with the booking object
        ResponseEntity<byte[]> response = abaPayService.qrImage(booking);
        return response != null ? response : ResponseEntity.internalServerError().build();
    }

    @PostMapping("callback")
    public ResponseEntity<Void> ExCallbackRequest(@RequestBody CallbackRequest request) {
        abaPayService.txnCallback(request);
        return ResponseEntity.ok().build();
    }
}
