package com.hotelbooking.service;

import com.hotelbooking.Repository.BookingRepository;
import com.hotelbooking.Repository.RoomRepository;
import com.hotelbooking.Repository.UserRepository;
import com.hotelbooking.model.Booking;
import com.hotelbooking.model.Room;
import com.hotelbooking.model.User;
import org.springframework.stereotype.Service;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;

    public BookingService(BookingRepository bookingRepository, UserRepository userRepository, RoomRepository roomRepository) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.roomRepository = roomRepository;
    }

    public Booking createBooking(Booking booking) {

        User user = userRepository.findById(booking.getUser().getId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + booking.getUser().getId()));

        Room room = roomRepository.findById(booking.getRoom().getId())
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + booking.getRoom().getId()));

        booking.setUser(user);
        booking.setRoom(room);

        // 4. Save the booking
        return bookingRepository.save(booking);
    }
}