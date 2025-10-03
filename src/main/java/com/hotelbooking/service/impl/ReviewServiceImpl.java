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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final HotelRepository hotelRepository;
    private final BookingRepository bookingRepository;

    @Override
    public ReviewDto createReview(ReviewDto reviewDto) {
        try {
            validateReviewDto(reviewDto);

            User user = userRepository.findById(reviewDto.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found with id: " + reviewDto.getUserId()));

            Hotel hotel = hotelRepository.findById(reviewDto.getHotelId())
                    .orElseThrow(() -> new RuntimeException("Hotel not found with id: " + reviewDto.getHotelId()));

            Booking booking = bookingRepository.findById(reviewDto.getBookingId())
                    .orElseThrow(() -> new RuntimeException("Booking not found with id: " + reviewDto.getBookingId()));

            // Validate that the user owns the booking
            if (!booking.getUser().getId().equals(user.getId())) {
                throw new RuntimeException("User can only review their own bookings");
            }

            Review review = new Review();
            review.setRating(convertRatingToBigDecimal(reviewDto.getRating()));
            review.setComment(reviewDto.getComment());
            review.setUser(user);
            review.setHotel(hotel);
            review.setBooking(booking);
            review.setCreatedAt(LocalDateTime.now());

            Review savedReview = reviewRepository.save(review);
            log.info("Review created successfully with ID: {}", savedReview.getId());

            return mapToDto(savedReview);
        } catch (Exception e) {
            log.error("Failed to create review: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create review: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewDto getReviewById(Long id) {
        try {
            Review review = reviewRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Review not found with id: " + id));
            return mapToDto(review);
        } catch (Exception e) {
            log.error("Failed to get review by id {}: {}", id, e.getMessage());
            throw new RuntimeException("Failed to retrieve review: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewDto> getAllReviews() {
        try {
            List<Review> reviews = reviewRepository.findAll();
            return reviews.stream()
                    .map(this::mapToDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to get all reviews: {}", e.getMessage());
            throw new RuntimeException("Failed to retrieve reviews: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewDto> getReviewsByHotelId(Long hotelId) {
        try {
            if (hotelId == null) {
                throw new IllegalArgumentException("Hotel ID cannot be null");
            }

            List<Review> reviews = reviewRepository.findByHotelId(hotelId);
            return reviews.stream()
                    .map(this::mapToDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to get reviews for hotel {}: {}", hotelId, e.getMessage());
            throw new RuntimeException("Failed to retrieve reviews: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewDto> getReviewsByUserId(Long userId) {
        try {
            if (userId == null) {
                throw new IllegalArgumentException("User ID cannot be null");
            }

            List<Review> reviews = reviewRepository.findByUserId(userId);
            return reviews.stream()
                    .map(this::mapToDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to get reviews for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to retrieve reviews: " + e.getMessage());
        }
    }

    @Override
    public ReviewDto updateReview(Long id, ReviewDto reviewDto) {
        try {
            Review existingReview = reviewRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Review not found with id: " + id));

            // Update only non-null fields
            if (reviewDto.getRating() != null) {
                existingReview.setRating(convertRatingToBigDecimal(reviewDto.getRating()));
            }
            if (reviewDto.getComment() != null) {
                existingReview.setComment(reviewDto.getComment().trim());
            }

            existingReview.setUpdatedAt(LocalDateTime.now());

            Review updatedReview = reviewRepository.save(existingReview);
            log.info("Review updated successfully with ID: {}", updatedReview.getId());

            return mapToDto(updatedReview);
        } catch (Exception e) {
            log.error("Failed to update review {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to update review: " + e.getMessage());
        }
    }

    @Override
    public void deleteReview(Long id) {
        try {
            if (!reviewRepository.existsById(id)) {
                throw new RuntimeException("Review not found with id: " + id);
            }
            reviewRepository.deleteById(id);
            log.info("Review deleted successfully with ID: {}", id);
        } catch (Exception e) {
            log.error("Failed to delete review {}: {}", id, e.getMessage());
            throw new RuntimeException("Failed to delete review: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Double getAverageRatingForHotel(Long hotelId) {
        try {
            // Use the repository method to get the average rating directly from the database
            Double averageRating = reviewRepository.getAverageRatingByHotelId(hotelId);
            return averageRating != null ? Math.round(averageRating * 100.0) / 100.0 : 0.0; // Round to 2 decimal places
        } catch (Exception e) {
            log.error("Failed to calculate average rating for hotel {}: {}", hotelId, e.getMessage());
            return 0.0;
        }
    }

    // Helper methods
    private ReviewDto mapToDto(Review review) {
        ReviewDto dto = new ReviewDto();
        dto.setId(review.getId());
        dto.setRating(review.getRating().toString());
        dto.setComment(review.getComment());

        if (review.getUser() != null) {
            dto.setUserId(review.getUser().getId());
            dto.setUserName(review.getUser().getFirstName() + " " + review.getUser().getLastName());
        }

        if (review.getHotel() != null) {
            dto.setHotelId(review.getHotel().getId());
            dto.setHotelName(review.getHotel().getName());
        }

        if (review.getBooking() != null) {
            dto.setBookingId(review.getBooking().getId());
        }

        dto.setCreatedAt(review.getCreatedAt());
        dto.setUpdatedAt(review.getUpdatedAt());

        return dto;
    }

    private BigDecimal convertRatingToBigDecimal(String rating) {
        try {
            BigDecimal ratingValue = new BigDecimal(rating);
            if (ratingValue.compareTo(BigDecimal.ONE) < 0 || ratingValue.compareTo(new BigDecimal("5")) > 0) {
                throw new IllegalArgumentException("Rating must be between 1 and 5");
            }
            return ratingValue;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid rating format: " + rating);
        }
    }

    private void validateReviewDto(ReviewDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Review data cannot be null");
        }
        if (dto.getRating() == null || dto.getRating().trim().isEmpty()) {
            throw new IllegalArgumentException("Rating cannot be null or empty");
        }
        if (dto.getComment() == null || dto.getComment().trim().isEmpty()) {
            throw new IllegalArgumentException("Comment cannot be null or empty");
        }
        if (dto.getUserId() == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (dto.getHotelId() == null) {
            throw new IllegalArgumentException("Hotel ID cannot be null");
        }
        if (dto.getBookingId() == null) {
            throw new IllegalArgumentException("Booking ID cannot be null");
        }

        // Validate rating value
        convertRatingToBigDecimal(dto.getRating()); // This will throw if invalid
    }
}