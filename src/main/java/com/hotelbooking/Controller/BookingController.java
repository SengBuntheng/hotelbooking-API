package com.hotelbooking.Controller;

import com.hotelbooking.dto.BookingCreationResponse;
import com.hotelbooking.model.Booking;
import com.hotelbooking.service.ABAPayService;
import com.hotelbooking.service.BookingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final ABAPayService abaPayService;

    public BookingController(BookingService bookingService, ABAPayService abaPayService) {
        this.bookingService = bookingService;
        this.abaPayService = abaPayService;
    }

    @PostMapping("/create")
    public ResponseEntity<BookingCreationResponse> createBookingAndGetQrCode(@RequestBody Booking booking) {
        // 1. Save the booking with an initial status
        booking.setBookingStatus(com.hotelbooking.Enum.BookingStatus.IN_PROGRESS);
        Booking savedBooking = bookingService.createBooking(booking);

        // 2. Generate the QR code as a Base64 string
        String qrCodeBase64 = abaPayService.getQrImageBase64(
                savedBooking.getTotalAmount().doubleValue(),
                "USD",
                String.valueOf(savedBooking.getId())
        );

        // 3. Create the structured JSON response
        BookingCreationResponse response = new BookingCreationResponse(savedBooking.getId(), qrCodeBase64);

        // 4. Return the response
        return ResponseEntity.ok(response);
    }
}
