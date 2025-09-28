package com.hotelbooking.service.impl;


import com.hotelbooking.Repository.HotelImageRepository;
import com.hotelbooking.Repository.HotelRepository;
import com.hotelbooking.dto.HotelImageDto;
import com.hotelbooking.dto.HotelDto;
import com.hotelbooking.model.Hotel;
import com.hotelbooking.model.HotelImage;
import com.hotelbooking.service.HotelImageService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class HotelImageServiceImpl implements HotelImageService {

    private final HotelImageRepository hotelImageRepository;
    private final HotelRepository hotelRepository;
    private final ModelMapper modelMapper;

    public HotelImageServiceImpl(HotelImageRepository hotelImageRepository,
                                 HotelRepository hotelRepository,
                                 ModelMapper modelMapper) {
        this.hotelImageRepository = hotelImageRepository;
        this.hotelRepository = hotelRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public HotelImageDto createHotelImage(HotelImageDto hotelImageDto) {
        if (hotelImageDto.getHotelId() == null) {
            throw new IllegalArgumentException("Hotel ID cannot be null");
        }

        Hotel hotel = hotelRepository.findById(hotelImageDto.getHotelId())
                .orElseThrow(() -> new RuntimeException("Hotel not found with id: " + hotelImageDto.getHotelId()));

        HotelImage hotelImage = modelMapper.map(hotelImageDto, HotelImage.class);
        hotelImage.setHotel(hotel);

        HotelImage savedHotelImage = hotelImageRepository.save(hotelImage);
        return mapHotelImageToDto(savedHotelImage);
    }

    @Override
    @Transactional(readOnly = true)
    public HotelImageDto getHotelImageById(Long id) {
        HotelImage hotelImage = hotelImageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("HotelImage not found with id: " + id));
        return mapHotelImageToDto(hotelImage);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HotelImageDto> getAllHotelImages() {
        return hotelImageRepository.findAll().stream()
                .map(this::mapHotelImageToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public List<HotelImageDto> getHotelImagesByHotelId(Long hotelId) {
        return hotelImageRepository.findByHotelId(hotelId).stream()
                .map(this::mapHotelImageToDto)
                .collect(Collectors.toList());
    }

    @Override
    public HotelImageDto updateHotelImage(Long id, HotelImageDto hotelImageDto) {
        HotelImage existingHotelImage = hotelImageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("HotelImage not found with id: " + id));

        if (hotelImageDto.getHotelId() == null) {
            throw new IllegalArgumentException("Hotel ID cannot be null");
        }

        Hotel hotel = hotelRepository.findById(hotelImageDto.getHotelId())
                .orElseThrow(() -> new RuntimeException("Hotel not found with id: " + hotelImageDto.getHotelId()));

        existingHotelImage.setImageUrl(hotelImageDto.getImageUrl());
        existingHotelImage.setImageType(hotelImageDto.getImageType());
        existingHotelImage.setAltText(hotelImageDto.getAltText());
        existingHotelImage.setIsPrimary(hotelImageDto.getIsPrimary());
        existingHotelImage.setHotel(hotel);

        HotelImage updatedHotelImage = hotelImageRepository.save(existingHotelImage);
        return mapHotelImageToDto(updatedHotelImage);
    }

    @Override
    public void deleteHotelImage(Long id) {
        if (!hotelImageRepository.existsById(id)) {
            throw new RuntimeException("HotelImage not found with id: " + id);
        }
        hotelImageRepository.deleteById(id);
    }

    // Helper method to map HotelImage to HotelImageDto
    private HotelImageDto mapHotelImageToDto(HotelImage hotelImage) {
        HotelImageDto dto = modelMapper.map(hotelImage, HotelImageDto.class);

        if (hotelImage.getHotel() != null) {
            dto.setHotelId(hotelImage.getHotel().getId());
            // Optionally include basic hotel info
            HotelDto hotelDto = HotelDto.builder()
                    .id(hotelImage.getHotel().getId())
                    .name(hotelImage.getHotel().getName())
                    .rating(hotelImage.getHotel().getRating())
                    .build();
            dto.setHotel(hotelDto);
        }

        return dto;
    }

}