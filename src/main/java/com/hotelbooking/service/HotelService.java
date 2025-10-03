package com.hotelbooking.service;

import com.hotelbooking.dto.HotelDto;
import java.util.List;

public interface HotelService {
    HotelDto createHotel(HotelDto hotelDto);
    HotelDto getHotelById(Long id);
    List<HotelDto> getAllHotels();
    List<HotelDto> getAllHotelsBasic(); // Add this missing method
    HotelDto updateHotel(Long id, HotelDto hotelDto);
    void deleteHotel(Long id);
}