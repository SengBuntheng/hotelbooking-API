package com.hotelbooking.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoomDto {
    private Long id;

    @NotBlank(message = "Room number is required")
    private String roomNumber;

    @NotBlank(message = "Room status is required")
    private String status;

    @NotNull(message = "Room type ID is required")
    private Long roomTypeId;

    // Nested objects for complete room information
    private RoomTypeDto roomType;
    private HotelDto hotel;
    private List<HotelImageDto> hotelImages;
}
