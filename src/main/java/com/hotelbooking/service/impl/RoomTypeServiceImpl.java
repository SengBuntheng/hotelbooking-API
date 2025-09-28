package com.hotelbooking.service.impl;


import com.hotelbooking.Repository.HotelRepository;
import com.hotelbooking.Repository.RoomTypeRepository;
import com.hotelbooking.dto.RoomTypeDto;
import com.hotelbooking.model.Hotel;
import com.hotelbooking.model.RoomType;
import com.hotelbooking.service.RoomTypeService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class RoomTypeServiceImpl implements RoomTypeService {

    private final RoomTypeRepository roomTypeRepository;
    private final HotelRepository hotelRepository;
    private final ModelMapper modelMapper;

    public RoomTypeServiceImpl(RoomTypeRepository roomTypeRepository,
                               HotelRepository hotelRepository,
                               ModelMapper modelMapper) {
        this.roomTypeRepository = roomTypeRepository;
        this.hotelRepository = hotelRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public RoomTypeDto createRoomType(RoomTypeDto roomTypeDto) {
        if (roomTypeDto.getHotelId() == null) {
            throw new IllegalArgumentException("Hotel ID cannot be null");
        }

        Hotel hotel = hotelRepository.findById(roomTypeDto.getHotelId())
                .orElseThrow(() -> new RuntimeException("Hotel not found with id: " + roomTypeDto.getHotelId()));

        RoomType roomType = modelMapper.map(roomTypeDto, RoomType.class);
        roomType.setHotel(hotel);
        roomType.setId(null); // Ensure new entity

        RoomType savedRoomType = roomTypeRepository.save(roomType);
        RoomTypeDto result = modelMapper.map(savedRoomType, RoomTypeDto.class);
        result.setHotelId(hotel.getId());
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public RoomTypeDto getRoomTypeById(Long id) {
        RoomType roomType = roomTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("RoomType not found with id: " + id));
        RoomTypeDto result = modelMapper.map(roomType, RoomTypeDto.class);
        if (roomType.getHotel() != null) {
            result.setHotelId(roomType.getHotel().getId());
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomTypeDto> getAllRoomTypes() {
        return roomTypeRepository.findAll().stream()
                .map(roomType -> {
                    RoomTypeDto dto = modelMapper.map(roomType, RoomTypeDto.class);
                    if (roomType.getHotel() != null) {
                        dto.setHotelId(roomType.getHotel().getId());
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public RoomTypeDto updateRoomType(Long id, RoomTypeDto roomTypeDto) {
        RoomType existingRoomType = roomTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("RoomType not found with id: " + id));

        if (roomTypeDto.getHotelId() == null) {
            throw new IllegalArgumentException("Hotel ID cannot be null");
        }

        Hotel hotel = hotelRepository.findById(roomTypeDto.getHotelId())
                .orElseThrow(() -> new RuntimeException("Hotel not found with id: " + roomTypeDto.getHotelId()));

        // Update only non-null fields
        if (roomTypeDto.getTypeName() != null) {
            existingRoomType.setTypeName(roomTypeDto.getTypeName());
        }
        if (roomTypeDto.getDescription() != null) {
            existingRoomType.setDescription(roomTypeDto.getDescription());
        }
        if (roomTypeDto.getPricePerNight() != null) {
            existingRoomType.setPricePerNight(roomTypeDto.getPricePerNight());
        }
        if (roomTypeDto.getMaxOccupancy() != null) {
            existingRoomType.setMaxOccupancy(roomTypeDto.getMaxOccupancy());
        }
        existingRoomType.setHotel(hotel);

        RoomType updatedRoomType = roomTypeRepository.save(existingRoomType);
        RoomTypeDto result = modelMapper.map(updatedRoomType, RoomTypeDto.class);
        result.setHotelId(hotel.getId());
        return result;
    }

    @Override
    public void deleteRoomType(Long id) {
        if (!roomTypeRepository.existsById(id)) {
            throw new RuntimeException("RoomType not found with id: " + id);
        }
        roomTypeRepository.deleteById(id);
    }
}
