package com.hotelbooking.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ReviewDto {
    private Long id;

    private Double rating;
    private String comment;
    private LocalDate reviewDate;
    private Long userId;
    private Long hotelId;
    private Long bookingId;
}