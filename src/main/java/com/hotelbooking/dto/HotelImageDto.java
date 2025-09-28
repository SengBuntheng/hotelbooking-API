package com.hotelbooking.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HotelImageDto {
    private Long id;

    @NotBlank(message = "Image URL is required")
    private String imageUrl;

    private String imageType;
    private String altText;
    private Boolean isPrimary;

    @NotNull(message = "Hotel ID is required")
    private Long hotelId;

    // Nested object
    private HotelDto hotel;
}