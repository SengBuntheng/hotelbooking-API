package com.hotelbooking.service.impl;


import com.hotelbooking.Repository.HotelImageRepository;
import com.hotelbooking.Repository.HotelRepository;
import com.hotelbooking.dto.HotelImageDto;
import com.hotelbooking.dto.HotelDto;
import com.hotelbooking.model.Hotel;
import com.hotelbooking.model.HotelImage;
import com.hotelbooking.service.HotelImageService;
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
public class HotelImageServiceImpl implements HotelImageService {

    private final HotelImageRepository hotelImageRepository;
    private final HotelRepository hotelRepository;

    @Override
    public HotelImageDto createHotelImage(HotelImageDto hotelImageDto) {
        try {
            validateHotelImageDto(hotelImageDto);

            Hotel hotel = hotelRepository.findById(hotelImageDto.getHotelId())
                    .orElseThrow(() -> new RuntimeException("Hotel not found with id: " + hotelImageDto.getHotelId()));

            HotelImage hotelImage = new HotelImage();
            hotelImage.setImageUrl(hotelImageDto.getImageUrl());
            hotelImage.setImageType(hotelImageDto.getImageType());
            hotelImage.setAltText(hotelImageDto.getAltText());
            hotelImage.setIsPrimary(hotelImageDto.getIsPrimary() != null ? hotelImageDto.getIsPrimary() : false);
            hotelImage.setHotel(hotel);
            hotelImage.setCreatedAt(LocalDateTime.now());

            HotelImage savedHotelImage = hotelImageRepository.save(hotelImage);
            log.info("Hotel image created successfully with ID: {}", savedHotelImage.getId());

            return mapToDto(savedHotelImage);
        } catch (Exception e) {
            log.error("Failed to create hotel image: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create hotel image: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public HotelImageDto getHotelImageById(Long id) {
        try {
            HotelImage hotelImage = hotelImageRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Hotel image not found with id: " + id));
            return mapToDto(hotelImage);
        } catch (Exception e) {
            log.error("Failed to get hotel image by id {}: {}", id, e.getMessage());
            throw new RuntimeException("Failed to retrieve hotel image: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<HotelImageDto> getAllHotelImages() {
        try {
            List<HotelImage> hotelImages = hotelImageRepository.findAll();
            return hotelImages.stream()
                    .map(this::mapToDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to get all hotel images: {}", e.getMessage());
            throw new RuntimeException("Failed to retrieve hotel images: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<HotelImageDto> getHotelImagesByHotelId(Long hotelId) {
        try {
            if (hotelId == null) {
                throw new IllegalArgumentException("Hotel ID cannot be null");
            }

            List<HotelImage> hotelImages = hotelImageRepository.findByHotelId(hotelId);
            return hotelImages.stream()
                    .map(this::mapToDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to get hotel images for hotel {}: {}", hotelId, e.getMessage());
            throw new RuntimeException("Failed to retrieve hotel images: " + e.getMessage());
        }
    }

    @Override
    public HotelImageDto updateHotelImage(Long id, HotelImageDto hotelImageDto) {
        try {
            validateHotelImageDto(hotelImageDto);

            HotelImage existingImage = hotelImageRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Hotel image not found with id: " + id));

            Hotel hotel = hotelRepository.findById(hotelImageDto.getHotelId())
                    .orElseThrow(() -> new RuntimeException("Hotel not found with id: " + hotelImageDto.getHotelId()));

            // Update fields
            existingImage.setImageUrl(hotelImageDto.getImageUrl());
            existingImage.setImageType(hotelImageDto.getImageType());
            existingImage.setAltText(hotelImageDto.getAltText());
            existingImage.setIsPrimary(hotelImageDto.getIsPrimary() != null ? hotelImageDto.getIsPrimary() : false);
            existingImage.setHotel(hotel);
            existingImage.setUpdatedAt(LocalDateTime.now());

            HotelImage updatedImage = hotelImageRepository.save(existingImage);
            log.info("Hotel image updated successfully with ID: {}", updatedImage.getId());

            return mapToDto(updatedImage);
        } catch (Exception e) {
            log.error("Failed to update hotel image {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to update hotel image: " + e.getMessage());
        }
    }

    @Override
    public void deleteHotelImage(Long id) {
        try {
            if (!hotelImageRepository.existsById(id)) {
                throw new RuntimeException("Hotel image not found with id: " + id);
            }
            hotelImageRepository.deleteById(id);
            log.info("Hotel image deleted successfully with ID: {}", id);
        } catch (Exception e) {
            log.error("Failed to delete hotel image {}: {}", id, e.getMessage());
            throw new RuntimeException("Failed to delete hotel image: " + e.getMessage());
        }
    }

    // Helper methods
    private HotelImageDto mapToDto(HotelImage hotelImage) {
        HotelImageDto dto = new HotelImageDto();
        dto.setId(hotelImage.getId());
        dto.setImageUrl(hotelImage.getImageUrl());
        dto.setImageType(hotelImage.getImageType());
        dto.setAltText(hotelImage.getAltText());
        dto.setIsPrimary(hotelImage.getIsPrimary());

        if (hotelImage.getHotel() != null) {
            dto.setHotelId(hotelImage.getHotel().getId());

            // Create basic hotel info for nested object
            HotelDto hotelDto = new HotelDto();
            hotelDto.setId(hotelImage.getHotel().getId());
            hotelDto.setName(hotelImage.getHotel().getName());
            hotelDto.setRating(hotelImage.getHotel().getRating());
            dto.setHotel(hotelDto);
        }

        return dto;
    }

    private void validateHotelImageDto(HotelImageDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Hotel image data cannot be null");
        }
        if (dto.getHotelId() == null) {
            throw new IllegalArgumentException("Hotel ID cannot be null");
        }
        if (dto.getImageUrl() == null || dto.getImageUrl().trim().isEmpty()) {
            throw new IllegalArgumentException("Image URL cannot be null or empty");
        }
    }
}