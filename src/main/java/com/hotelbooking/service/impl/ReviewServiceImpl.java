package com.hotelbooking.service.impl;

import com.hotelbooking.Repository.BookingRepository;
import com.hotelbooking.Repository.HotelRepository;
import com.hotelbooking.Repository.ReviewRepository;
import com.hotelbooking.Repository.UserRepository;
import com.hotelbooking.dto.ReviewDto;
import com.hotelbooking.model.Booking;
import com.hotelbooking.model.Hotel;
import com.hotelbooking.model.Review;
import com.hotelbooking.model.User;
import com.hotelbooking.service.ReviewService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final HotelRepository hotelRepository;
    private final BookingRepository bookingRepository;
    private final ModelMapper modelMapper;

    public ReviewServiceImpl(ReviewRepository reviewRepository, UserRepository userRepository, HotelRepository hotelRepository, BookingRepository bookingRepository, ModelMapper modelMapper) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.hotelRepository = hotelRepository;
        this.bookingRepository = bookingRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public ReviewDto createReview(ReviewDto reviewDto) {
        User user = userRepository.findById(reviewDto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + reviewDto.getUserId()));

        Hotel hotel = hotelRepository.findById(reviewDto.getHotelId())
                .orElseThrow(() -> new RuntimeException("Hotel not found with id: " + reviewDto.getHotelId()));

        Booking booking = bookingRepository.findById(reviewDto.getBookingId())
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + reviewDto.getBookingId()));

        Review review = modelMapper.map(reviewDto, Review.class);
        review.setUser(user);
        review.setHotel(hotel);
        review.setBooking(booking);

        Review savedReview = reviewRepository.save(review);
        return modelMapper.map(savedReview, ReviewDto.class);
    }

    @Override
    public ReviewDto getReviewById(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found with id: " + id));
        return modelMapper.map(review, ReviewDto.class);
    }

    @Override
    public List<ReviewDto> getAllReviews() {
        return reviewRepository.findAll().stream()
                .map(review -> modelMapper.map(review, ReviewDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<ReviewDto> getReviewsByHotelId(Long hotelId) {
        return reviewRepository.findByHotelId(hotelId).stream()
                .map(review -> modelMapper.map(review, ReviewDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public ReviewDto updateReview(Long id, ReviewDto reviewDto) {
        Review existingReview = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found with id: " + id));

        existingReview.setRating(reviewDto.getRating());
        existingReview.setComment(reviewDto.getComment());

        Review updatedReview = reviewRepository.save(existingReview);
        return modelMapper.map(updatedReview, ReviewDto.class);
    }

    @Override
    public void deleteReview(Long id) {
        if (!reviewRepository.existsById(id)) {
            throw new RuntimeException("Review not found with id: " + id);
        }
        reviewRepository.deleteById(id);
    }
}