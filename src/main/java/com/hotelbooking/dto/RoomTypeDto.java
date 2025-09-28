package com.hotelbooking.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoomTypeDto {
    private Long id;

    @NotBlank(message = "Type name is required")
    private String typeName;

    private String description;

    @NotNull(message = "Price per night is required")
    @Positive(message = "Price must be positive")
    private BigDecimal pricePerNight;

    @NotNull(message = "Max occupancy is required")
    @Positive(message = "Max occupancy must be positive")
    private Integer maxOccupancy;

    @NotNull(message = "Hotel ID is required")
    private Long hotelId;

    // Nested objects
    private HotelDto hotel;
    private List<RoomDto> rooms;
}