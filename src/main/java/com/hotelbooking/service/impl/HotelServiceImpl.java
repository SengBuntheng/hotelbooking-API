package com.hotelbooking.service.impl;


import com.hotelbooking.Repository.HotelRepository;
import com.hotelbooking.dto.HotelDto;
import com.hotelbooking.dto.HotelImageDto;
import com.hotelbooking.model.Hotel;
import com.hotelbooking.service.HotelService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class HotelServiceImpl implements HotelService {

    private final HotelRepository hotelRepository;
    private final ModelMapper modelMapper;

    public HotelServiceImpl(HotelRepository hotelRepository, ModelMapper modelMapper) {
        this.hotelRepository = hotelRepository;
        this.modelMapper = modelMapper;
    }
    private HotelDto mapHotelToDto(Hotel hotel) {
        HotelDto hotelDto = modelMapper.map(hotel, HotelDto.class);

        if (hotel.getHotelImages() != null) {
            hotelDto.setHotelImages(
                    hotel.getHotelImages().stream()
                            .map(image -> {
                                HotelImageDto imgDto = modelMapper.map(image, HotelImageDto.class);
                                imgDto.setHotelId(hotel.getId());
                                return imgDto;
                            }).collect(Collectors.toList())
            );
        }

        if (hotel.getRoomTypes() != null) {
            hotelDto.setRoomTypes(
                    hotel.getRoomTypes().stream()
                            .map(roomType -> {
                                var roomTypeDto = modelMapper.map(roomType, com.hotelbooking.dto.RoomTypeDto.class);
                                roomTypeDto.setHotelId(hotel.getId());
                                return roomTypeDto;
                            }).collect(Collectors.toList())
            );
        }

        return hotelDto;
    }
        @Override
    public HotelDto createHotel(HotelDto hotelDto) {
        Hotel hotel = modelMapper.map(hotelDto, Hotel.class);
        hotel.setId(null);
        Hotel savedHotel = hotelRepository.save(hotel);
        return mapHotelToDto(savedHotel);
    }
    @Override
    @Transactional(readOnly = true)
    public HotelDto getHotelById(Long id) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hotel not found with id: " + id));
        return mapHotelToDto(hotel);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HotelDto> getAllHotels() {
        List<Hotel> hotels = hotelRepository.findAll();

        return hotels.stream()
                .map(this::mapHotelToDto)
                .collect(Collectors.toList());
    }

    @Override
    public HotelDto updateHotel(Long id, HotelDto hotelDto) {
        Hotel existingHotel = hotelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hotel not found with id: " + id));

        if (hotelDto.getName() != null) {
            existingHotel.setName(hotelDto.getName());
        }
        if (hotelDto.getEmail() != null) {
            existingHotel.setEmail(hotelDto.getEmail());
        }
        if (hotelDto.getPhone() != null) {
            existingHotel.setPhone(hotelDto.getPhone());
        }
        if (hotelDto.getDescription() != null) {
            existingHotel.setDescription(hotelDto.getDescription());
        }
        if (hotelDto.getRating() != null) {
            existingHotel.setRating(hotelDto.getRating());
        }

        Hotel updatedHotel = hotelRepository.save(existingHotel);
        return mapHotelToDto(updatedHotel);
    }
    @Override
    public void deleteHotel(Long id) {
        if (!hotelRepository.existsById(id)) {
            throw new RuntimeException("Hotel not found with id: " + id);
        }
        hotelRepository.deleteById(id);
    }
}
