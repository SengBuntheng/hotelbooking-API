package com.hotelbooking.service.impl;

import com.hotelbooking.Repository.HotelRepository;
import com.hotelbooking.Repository.RoomTypeRepository;
import com.hotelbooking.dto.RoomTypeDto;
import com.hotelbooking.dto.HotelDto;
import com.hotelbooking.model.Hotel;
import com.hotelbooking.model.RoomType;
import com.hotelbooking.service.RoomTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class RoomTypeServiceImpl implements RoomTypeService {

    private final RoomTypeRepository roomTypeRepository;
    private final HotelRepository hotelRepository;

    @Override
    public RoomTypeDto createRoomType(RoomTypeDto roomTypeDto) {
        try {
            validateRoomTypeDto(roomTypeDto);

            Hotel hotel = hotelRepository.findById(roomTypeDto.getHotelId())
                    .orElseThrow(() -> new RuntimeException("Hotel not found with id: " + roomTypeDto.getHotelId()));

            RoomType roomType = new RoomType();
            roomType.setTypeName(roomTypeDto.getTypeName());
            roomType.setDescription(roomTypeDto.getDescription());
            roomType.setPricePerNight(roomTypeDto.getPricePerNight());
            roomType.setMaxOccupancy(roomTypeDto.getMaxOccupancy());
            roomType.setHotel(hotel);
            roomType.setCreatedAt(LocalDateTime.now());

            RoomType savedRoomType = roomTypeRepository.save(roomType);
            log.info("Room type created successfully with ID: {}", savedRoomType.getId());

            return mapToDto(savedRoomType, false);
        } catch (Exception e) {
            log.error("Failed to create room type: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create room type: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public RoomTypeDto getRoomTypeById(Long id) {
        try {
            RoomType roomType = roomTypeRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Room type not found with id: " + id));
            return mapToDto(roomType, true);
        } catch (Exception e) {
            log.error("Failed to get room type by id {}: {}", id, e.getMessage());
            throw new RuntimeException("Failed to retrieve room type: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomTypeDto> getAllRoomTypes() {
        try {
            List<RoomType> roomTypes = roomTypeRepository.findAll();
            return roomTypes.stream()
                    .map(roomType -> mapToDto(roomType, true))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to get all room types: {}", e.getMessage());
            throw new RuntimeException("Failed to retrieve room types: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomTypeDto> getRoomTypesByHotelId(Long hotelId) {
        try {
            if (hotelId == null) {
                throw new IllegalArgumentException("Hotel ID cannot be null");
            }

            List<RoomType> roomTypes = roomTypeRepository.findByHotelId(hotelId);
            return roomTypes.stream()
                    .map(roomType -> mapToDto(roomType, false)) // Basic mapping without nested hotel
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to get room types for hotel {}: {}", hotelId, e.getMessage());
            throw new RuntimeException("Failed to retrieve room types: " + e.getMessage());
        }
    }

    @Override
    public RoomTypeDto updateRoomType(Long id, RoomTypeDto roomTypeDto) {
        try {
            RoomType existingRoomType = roomTypeRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Room type not found with id: " + id));

            // Update only non-null fields
            if (roomTypeDto.getTypeName() != null && !roomTypeDto.getTypeName().trim().isEmpty()) {
                existingRoomType.setTypeName(roomTypeDto.getTypeName().trim());
            }
            if (roomTypeDto.getDescription() != null) {
                existingRoomType.setDescription(roomTypeDto.getDescription().trim());
            }
            if (roomTypeDto.getPricePerNight() != null) {
                existingRoomType.setPricePerNight(roomTypeDto.getPricePerNight());
            }
            if (roomTypeDto.getMaxOccupancy() != null) {
                existingRoomType.setMaxOccupancy(roomTypeDto.getMaxOccupancy());
            }
            if (roomTypeDto.getHotelId() != null) {
                Hotel hotel = hotelRepository.findById(roomTypeDto.getHotelId())
                        .orElseThrow(() -> new RuntimeException("Hotel not found with id: " + roomTypeDto.getHotelId()));
                existingRoomType.setHotel(hotel);
            }

            existingRoomType.setUpdatedAt(LocalDateTime.now());

            RoomType updatedRoomType = roomTypeRepository.save(existingRoomType);
            log.info("Room type updated successfully with ID: {}", updatedRoomType.getId());

            return mapToDto(updatedRoomType, true);
        } catch (Exception e) {
            log.error("Failed to update room type {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to update room type: " + e.getMessage());
        }
    }

    @Override
    public void deleteRoomType(Long id) {
        try {
            if (!roomTypeRepository.existsById(id)) {
                throw new RuntimeException("Room type not found with id: " + id);
            }
            roomTypeRepository.deleteById(id);
            log.info("Room type deleted successfully with ID: {}", id);
        } catch (Exception e) {
            log.error("Failed to delete room type {}: {}", id, e.getMessage());
            throw new RuntimeException("Failed to delete room type: " + e.getMessage());
        }
    }

    // Helper methods
    private RoomTypeDto mapToDto(RoomType roomType, boolean includeHotel) {
        RoomTypeDto dto = new RoomTypeDto();
        dto.setId(roomType.getId());
        dto.setTypeName(roomType.getTypeName());
        dto.setDescription(roomType.getDescription());
        dto.setPricePerNight(roomType.getPricePerNight());
        dto.setMaxOccupancy(roomType.getMaxOccupancy());

        if (roomType.getHotel() != null) {
            dto.setHotelId(roomType.getHotel().getId());

            if (includeHotel) {
                HotelDto hotelDto = new HotelDto();
                hotelDto.setId(roomType.getHotel().getId());
                hotelDto.setName(roomType.getHotel().getName());
                hotelDto.setEmail(roomType.getHotel().getEmail());
                hotelDto.setPhone(roomType.getHotel().getPhone());
                hotelDto.setDescription(roomType.getHotel().getDescription());
                hotelDto.setAddress(roomType.getHotel().getAddress());
                hotelDto.setCity(roomType.getHotel().getCity());
                hotelDto.setCountry(roomType.getHotel().getCountry());
                hotelDto.setRating(roomType.getHotel().getRating());
                dto.setHotel(hotelDto);
            }
        }

        return dto;
    }

    private void validateRoomTypeDto(RoomTypeDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Room type data cannot be null");
        }
        if (dto.getTypeName() == null || dto.getTypeName().trim().isEmpty()) {
            throw new IllegalArgumentException("Room type name cannot be null or empty");
        }
        if (dto.getPricePerNight() == null || dto.getPricePerNight().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price per night must be greater than zero");
        }
        if (dto.getMaxOccupancy() == null || dto.getMaxOccupancy() <= 0) {
            throw new IllegalArgumentException("Max occupancy must be greater than zero");
        }
        if (dto.getHotelId() == null) {
            throw new IllegalArgumentException("Hotel ID cannot be null");
        }
    }
}