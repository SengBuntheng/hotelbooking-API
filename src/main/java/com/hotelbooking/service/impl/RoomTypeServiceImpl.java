package com.hotelbooking.service.impl;

import com.hotelbooking.Repository.HotelRepository;
import com.hotelbooking.Repository.RoomTypeRepository;
import com.hotelbooking.dto.RoomTypeDto;
import com.hotelbooking.model.Hotel;
import com.hotelbooking.model.RoomType;
import com.hotelbooking.service.RoomTypeService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoomTypeServiceImpl implements RoomTypeService {

    private final RoomTypeRepository roomTypeRepository;
    private final HotelRepository hotelRepository;
    private final ModelMapper modelMapper;

    public RoomTypeServiceImpl(RoomTypeRepository roomTypeRepository, HotelRepository hotelRepository, ModelMapper modelMapper) {
        this.roomTypeRepository = roomTypeRepository;
        this.hotelRepository = hotelRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public RoomTypeDto createRoomType(RoomTypeDto roomTypeDto) {
        Hotel hotel = hotelRepository.findById(roomTypeDto.getHotelId())
                .orElseThrow(() -> new RuntimeException("Hotel not found with id: " + roomTypeDto.getHotelId()));

        RoomType roomType = modelMapper.map(roomTypeDto, RoomType.class);
        roomType.setHotel(hotel);

        RoomType savedRoomType = roomTypeRepository.save(roomType);
        return modelMapper.map(savedRoomType, RoomTypeDto.class);
    }

    @Override
    public RoomTypeDto getRoomTypeById(Long id) {
        RoomType roomType = roomTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("RoomType not found with id: " + id));
        return modelMapper.map(roomType, RoomTypeDto.class);
    }

    @Override
    public List<RoomTypeDto> getAllRoomTypes() {
        return roomTypeRepository.findAll().stream()
                .map(roomType -> modelMapper.map(roomType, RoomTypeDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public RoomTypeDto updateRoomType(Long id, RoomTypeDto roomTypeDto) {
        RoomType existingRoomType = roomTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("RoomType not found with id: " + id));

        Hotel hotel = hotelRepository.findById(roomTypeDto.getHotelId())
                .orElseThrow(() -> new RuntimeException("Hotel not found with id: " + roomTypeDto.getHotelId()));

        existingRoomType.setTypeName(roomTypeDto.getTypeName());
        existingRoomType.setDescription(roomTypeDto.getDescription());
        existingRoomType.setPricePerNight(roomTypeDto.getPricePerNight());
        existingRoomType.setMaxOccupancy(roomTypeDto.getMaxOccupancy());
        existingRoomType.setHotel(hotel);

        RoomType updatedRoomType = roomTypeRepository.save(existingRoomType);
        return modelMapper.map(updatedRoomType, RoomTypeDto.class);
    }

    @Override
    public void deleteRoomType(Long id) {
        if (!roomTypeRepository.existsById(id)) {
            throw new RuntimeException("RoomType not found with id: " + id);
        }
        roomTypeRepository.deleteById(id);
    }
}