package com.hotelbooking.service.impl;

import com.hotelbooking.Repository.HotelImageRepository;
import com.hotelbooking.Repository.HotelRepository;
import com.hotelbooking.Repository.RoomTypeRepository;
import com.hotelbooking.dto.HotelDto;
import com.hotelbooking.dto.HotelImageDto;
import com.hotelbooking.dto.RoomTypeDto;
import com.hotelbooking.model.Hotel;
import com.hotelbooking.model.HotelImage;
import com.hotelbooking.model.RoomType;
import com.hotelbooking.service.HotelService;
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
public class HotelServiceImpl implements HotelService {

    private final HotelRepository hotelRepository;
    private final HotelImageRepository hotelImageRepository;
    private final RoomTypeRepository roomTypeRepository;

    @Override
    public HotelDto createHotel(HotelDto hotelDto) {
        try {
            validateHotelDto(hotelDto);

            Hotel hotel = new Hotel();
            hotel.setName(hotelDto.getName());
            hotel.setEmail(hotelDto.getEmail());
            hotel.setPhone(hotelDto.getPhone());
            hotel.setDescription(hotelDto.getDescription());
            hotel.setAddress(hotelDto.getAddress());
            hotel.setCity(hotelDto.getCity());
            hotel.setCountry(hotelDto.getCountry());
            hotel.setRating(hotelDto.getRating());
            hotel.setCreatedAt(LocalDateTime.now());

            Hotel savedHotel = hotelRepository.save(hotel);
            log.info("Hotel created successfully with ID: {}", savedHotel.getId());

            return mapToDto(savedHotel, false); // Basic mapping for creation
        } catch (Exception e) {
            log.error("Failed to create hotel: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create hotel: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public HotelDto getHotelById(Long id) {
        try {
            Hotel hotel = hotelRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Hotel not found with id: " + id));
            return mapToDto(hotel, true); // Include nested objects
        } catch (Exception e) {
            log.error("Failed to get hotel by id {}: {}", id, e.getMessage());
            throw new RuntimeException("Failed to retrieve hotel: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<HotelDto> getAllHotels() {
        try {
            List<Hotel> hotels = hotelRepository.findAll();
            return hotels.stream()
                    .map(hotel -> mapToDto(hotel, true)) // Include nested objects
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to get all hotels: {}", e.getMessage());
            throw new RuntimeException("Failed to retrieve hotels: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<HotelDto> getAllHotelsBasic() {
        try {
            List<Hotel> hotels = hotelRepository.findAll();
            return hotels.stream()
                    .map(hotel -> mapToDto(hotel, false)) // Basic mapping only
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to get all hotels (basic): {}", e.getMessage());
            throw new RuntimeException("Failed to retrieve hotels: " + e.getMessage());
        }
    }

    @Override
    public HotelDto updateHotel(Long id, HotelDto hotelDto) {
        try {
            Hotel existingHotel = hotelRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Hotel not found with id: " + id));

            // Update only non-null fields
            if (hotelDto.getName() != null && !hotelDto.getName().trim().isEmpty()) {
                existingHotel.setName(hotelDto.getName().trim());
            }
            if (hotelDto.getEmail() != null && !hotelDto.getEmail().trim().isEmpty()) {
                existingHotel.setEmail(hotelDto.getEmail().trim());
            }
            if (hotelDto.getPhone() != null && !hotelDto.getPhone().trim().isEmpty()) {
                existingHotel.setPhone(hotelDto.getPhone().trim());
            }
            if (hotelDto.getDescription() != null) {
                existingHotel.setDescription(hotelDto.getDescription().trim());
            }
            if (hotelDto.getAddress() != null) {
                existingHotel.setAddress(hotelDto.getAddress().trim());
            }
            if (hotelDto.getCity() != null) {
                existingHotel.setCity(hotelDto.getCity().trim());
            }
            if (hotelDto.getCountry() != null) {
                existingHotel.setCountry(hotelDto.getCountry().trim());
            }
            if (hotelDto.getRating() != null) {
                existingHotel.setRating(hotelDto.getRating());
            }

            existingHotel.setUpdatedAt(LocalDateTime.now());

            Hotel updatedHotel = hotelRepository.save(existingHotel);
            log.info("Hotel updated successfully with ID: {}", updatedHotel.getId());

            return mapToDto(updatedHotel, true);
        } catch (Exception e) {
            log.error("Failed to update hotel {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to update hotel: " + e.getMessage());
        }
    }

    @Override
    public void deleteHotel(Long id) {
        try {
            if (!hotelRepository.existsById(id)) {
                throw new RuntimeException("Hotel not found with id: " + id);
            }
            hotelRepository.deleteById(id);
            log.info("Hotel deleted successfully with ID: {}", id);
        } catch (Exception e) {
            log.error("Failed to delete hotel {}: {}", id, e.getMessage());
            throw new RuntimeException("Failed to delete hotel: " + e.getMessage());
        }
    }

    // Helper methods
    private HotelDto mapToDto(Hotel hotel, boolean includeNestedObjects) {
        HotelDto dto = new HotelDto();
        dto.setId(hotel.getId());
        dto.setName(hotel.getName());
        dto.setEmail(hotel.getEmail());
        dto.setPhone(hotel.getPhone());
        dto.setDescription(hotel.getDescription());
        dto.setAddress(hotel.getAddress());
        dto.setCity(hotel.getCity());
        dto.setCountry(hotel.getCountry());
        dto.setRating(hotel.getRating());

        if (includeNestedObjects) {
            // Load hotel images
            List<HotelImage> images = hotelImageRepository.findByHotelId(hotel.getId());
            if (!images.isEmpty()) {
                List<HotelImageDto> imageDtos = images.stream()
                        .map(this::mapImageToDto)
                        .collect(Collectors.toList());
                dto.setHotelImages(imageDtos);
            }

            // Load room types
            List<RoomType> roomTypes = roomTypeRepository.findByHotelId(hotel.getId());
            if (!roomTypes.isEmpty()) {
                List<RoomTypeDto> roomTypeDtos = roomTypes.stream()
                        .map(this::mapRoomTypeToDto)
                        .collect(Collectors.toList());
                dto.setRoomTypes(roomTypeDtos);
            }
        }

        return dto;
    }

    private HotelImageDto mapImageToDto(HotelImage image) {
        HotelImageDto dto = new HotelImageDto();
        dto.setId(image.getId());
        dto.setImageUrl(image.getImageUrl());
        dto.setImageType(image.getImageType());
        dto.setAltText(image.getAltText());
        dto.setIsPrimary(image.getIsPrimary());
        dto.setHotelId(image.getHotel().getId());
        return dto;
    }

    private RoomTypeDto mapRoomTypeToDto(RoomType roomType) {
        RoomTypeDto dto = new RoomTypeDto();
        dto.setId(roomType.getId());
        dto.setTypeName(roomType.getTypeName());
        dto.setDescription(roomType.getDescription());
        dto.setPricePerNight(roomType.getPricePerNight());
        dto.setMaxOccupancy(roomType.getMaxOccupancy());
        dto.setHotelId(roomType.getHotel().getId());
        return dto;
    }

    private void validateHotelDto(HotelDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Hotel data cannot be null");
        }
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Hotel name cannot be null or empty");
        }
    }
}