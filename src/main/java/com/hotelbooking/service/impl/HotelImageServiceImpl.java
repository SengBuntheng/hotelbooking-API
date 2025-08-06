package com.hotelbooking.service.impl;

import com.hotelbooking.Repository.HotelImageRepository;
import com.hotelbooking.Repository.HotelRepository;
import com.hotelbooking.dto.HotelImageDto;
import com.hotelbooking.model.Hotel;
import com.hotelbooking.model.HotelImage;
import com.hotelbooking.service.HotelImageService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class HotelImageServiceImpl implements HotelImageService {

    private final HotelImageRepository hotelImageRepository;
    private final HotelRepository hotelRepository;
    private final ModelMapper modelMapper;

    public HotelImageServiceImpl(HotelImageRepository hotelImageRepository, HotelRepository hotelRepository, ModelMapper modelMapper) {
        this.hotelImageRepository = hotelImageRepository;
        this.hotelRepository = hotelRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public HotelImageDto createHotelImage(HotelImageDto hotelImageDto) {
        Hotel hotel = hotelRepository.findById(hotelImageDto.getHotelId())
                .orElseThrow(() -> new RuntimeException("Hotel not found with id: " + hotelImageDto.getHotelId()));

        HotelImage hotelImage = modelMapper.map(hotelImageDto, HotelImage.class);
        hotelImage.setHotel(hotel);

        HotelImage savedHotelImage = hotelImageRepository.save(hotelImage);
        return modelMapper.map(savedHotelImage, HotelImageDto.class);
    }

    @Override
    public HotelImageDto getHotelImageById(Long id) {
        HotelImage hotelImage = hotelImageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("HotelImage not found with id: " + id));
        return modelMapper.map(hotelImage, HotelImageDto.class);
    }

    @Override
    public List<HotelImageDto> getAllHotelImages() {
        return hotelImageRepository.findAll().stream()
                .map(hotelImage -> modelMapper.map(hotelImage, HotelImageDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public HotelImageDto updateHotelImage(Long id, HotelImageDto hotelImageDto) {
        HotelImage existingHotelImage = hotelImageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("HotelImage not found with id: " + id));

        Hotel hotel = hotelRepository.findById(hotelImageDto.getHotelId())
                .orElseThrow(() -> new RuntimeException("Hotel not found with id: " + hotelImageDto.getHotelId()));

        existingHotelImage.setImageUrl(hotelImageDto.getImageUrl());
        existingHotelImage.setImageType(hotelImageDto.getImageType());
        existingHotelImage.setHotel(hotel);

        HotelImage updatedHotelImage = hotelImageRepository.save(existingHotelImage);
        return modelMapper.map(updatedHotelImage, HotelImageDto.class);
    }

    @Override
    public void deleteHotelImage(Long id) {
        if (!hotelImageRepository.existsById(id)) {
            throw new RuntimeException("HotelImage not found with id: " + id);
        }
        hotelImageRepository.deleteById(id);
    }
}