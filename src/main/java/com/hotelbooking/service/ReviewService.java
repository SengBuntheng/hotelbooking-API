package com.hotelbooking.service;

import com.hotelbooking.dto.ReviewDto;
import java.util.List;

public interface ReviewService {
    ReviewDto createReview(ReviewDto reviewDto);
    ReviewDto getReviewById(Long id);
    List<ReviewDto> getAllReviews();
    List<ReviewDto> getReviewsByHotelId(Long hotelId);
    ReviewDto updateReview(Long id, ReviewDto reviewDto);
    void deleteReview(Long id);
}