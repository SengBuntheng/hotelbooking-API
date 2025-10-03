package com.hotelbooking.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReviewDto {
    private Long id;

    @NotBlank(message = "Rating is required")
    @DecimalMin(value = "1.0", message = "Rating must be at least 1.0")
    @DecimalMax(value = "5.0", message = "Rating must not exceed 5.0")
    private String rating;

    @NotBlank(message = "Comment is required")
    private String comment;

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Hotel ID is required")
    private Long hotelId;

    @NotNull(message = "Booking ID is required")
    private Long bookingId;

    private String userName;
    private String hotelName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}