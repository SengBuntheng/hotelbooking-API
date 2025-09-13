package com.hotelbooking.service;

import com.hotelbooking.Repository.BookingRepository;
import com.hotelbooking.Repository.RoomRepository;
import com.hotelbooking.Repository.UserRepository;
import com.hotelbooking.model.Booking;
import com.hotelbooking.model.Room;
import com.hotelbooking.model.User;
import org.springframework.stereotype.Service;

import java.util.List;

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

        if (booking.getUser() == null || booking.getUser().getId() == 0) {
            throw new IllegalArgumentException("User ID must be provided in the booking request.");
        }
        if (booking.getRoom() == null || booking.getRoom().getId() == 0) {
            throw new IllegalArgumentException("Room ID must be provided in the booking request.");
        }

        User user = userRepository.findById(booking.getUser().getId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + booking.getUser().getId()));

        Room room = roomRepository.findById(booking.getRoom().getId())
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + booking.getRoom().getId()));

        booking.setUser(user);
        booking.setRoom(room);


        return bookingRepository.save(booking);
    }
   public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

}