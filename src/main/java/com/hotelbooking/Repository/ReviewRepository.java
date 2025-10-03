package com.hotelbooking.Repository;

import com.hotelbooking.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByHotelId(Long hotelId);

    List<Review> findByUserId(Long userId);

    List<Review> findByBookingId(Long bookingId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.hotel.id = :hotelId")
    Double getAverageRatingByHotelId(@Param("hotelId") Long hotelId);

    @Query("SELECT r FROM Review r WHERE r.hotel.id = :hotelId ORDER BY r.createdAt DESC")
    List<Review> findByHotelIdOrderByCreatedAtDesc(@Param("hotelId") Long hotelId);
}