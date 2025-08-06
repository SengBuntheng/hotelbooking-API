package com.hotelbooking.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ReviewDto {
    private Long id;
    private Integer rating;
    private String comment;
    private LocalDate reviewDate;
    private Long userId;
    private Long hotelId;
    private Long bookingId;
}