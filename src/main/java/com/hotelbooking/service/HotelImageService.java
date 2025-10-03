package com.hotelbooking.service;

import com.hotelbooking.dto.HotelImageDto;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public interface HotelImageService {
    HotelImageDto createHotelImage(HotelImageDto hotelImageDto);
    HotelImageDto getHotelImageById(Long id);
    List<HotelImageDto> getAllHotelImages();

    @Transactional(readOnly = true)
    List<HotelImageDto> getHotelImagesByHotelId(Long hotelId);

    HotelImageDto updateHotelImage(Long id, HotelImageDto hotelImageDto);
    void deleteHotelImage(Long id);
}