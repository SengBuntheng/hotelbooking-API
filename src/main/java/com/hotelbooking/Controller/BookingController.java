package com.hotelbooking.Controller;

import com.hotelbooking.dto.BookingCreationResponse;
import com.hotelbooking.model.Booking;
import com.hotelbooking.service.ABAPayService;
import com.hotelbooking.service.BookingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
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
        try {
            booking.setBookingStatus(com.hotelbooking.Enum.BookingStatus.IN_PROGRESS);
            Booking savedBooking = bookingService.createBooking(booking);

            String qrCodeBase64 = abaPayService.getQrImageBase64(savedBooking);

            BookingCreationResponse response = new BookingCreationResponse(savedBooking.getId(), qrCodeBase64);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Log the exception here
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<Booking>> getAllBookings() {
        try {
            List<Booking> bookings = bookingService.getAllBookings();
            return ResponseEntity.ok(bookings);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}