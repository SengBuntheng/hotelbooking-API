package com.hotelbooking.service;

import com.hotelbooking.dto.HotelImageDto;
import java.util.List;

public interface HotelImageService {
    HotelImageDto createHotelImage(HotelImageDto hotelImageDto);
    HotelImageDto getHotelImageById(Long id);
    List<HotelImageDto> getAllHotelImages();
    HotelImageDto updateHotelImage(Long id, HotelImageDto hotelImageDto);
    void deleteHotelImage(Long id);
}