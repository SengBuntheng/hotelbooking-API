package com.hotelbooking.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hotelbooking.model.HotelAmenity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HotelDto {
    private Long id;

    @NotBlank(message = "Hotel name is required")
    private String name;

    @Email(message = "Invalid email format")
    private String email;

    private String phone;
    private String description;
    private String address;
    private String city;
    private String country;


    @DecimalMin("0.0")
    @DecimalMax("5.0")
    private BigDecimal rating;

    // Nested objects
    private List<RoomTypeDto> roomTypes;
    private List<HotelImageDto> hotelImages;
    private List<RoomDto> rooms;
    private List<StaffDto> staff;

    private List<HotelAmenity> hotelAmenities;
}