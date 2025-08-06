package com.hotelbooking.service.impl;

import com.hotelbooking.Repository.HotelRepository;
import com.hotelbooking.dto.HotelDto;
import com.hotelbooking.model.Hotel;
import com.hotelbooking.service.HotelService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class HotelServiceImpl implements HotelService {

    private final HotelRepository hotelRepository;
    private final ModelMapper modelMapper;

    public HotelServiceImpl(HotelRepository hotelRepository, ModelMapper modelMapper) {
        this.hotelRepository = hotelRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public HotelDto createHotel(HotelDto hotelDto) {
        Hotel hotel = modelMapper.map(hotelDto, Hotel.class);
        Hotel savedHotel = hotelRepository.save(hotel);
        return modelMapper.map(savedHotel, HotelDto.class);
    }

    @Override
    public HotelDto getHotelById(Long id) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hotel not found with id: " + id));
        return modelMapper.map(hotel, HotelDto.class);
    }

    @Override
    public List<HotelDto> getAllHotels() {
        return hotelRepository.findAll().stream()
                .map(hotel -> modelMapper.map(hotel, HotelDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public HotelDto updateHotel(Long id, HotelDto hotelDto) {
        Hotel existingHotel = hotelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hotel not found with id: " + id));

        existingHotel.setName(hotelDto.getName());
        existingHotel.setEmail(hotelDto.getEmail());
        existingHotel.setPhone(hotelDto.getPhone());
        existingHotel.setDescription(hotelDto.getDescription());
        existingHotel.setRating(hotelDto.getRating());

        Hotel updatedHotel = hotelRepository.save(existingHotel);
        return modelMapper.map(updatedHotel, HotelDto.class);
    }

    @Override
    public void deleteHotel(Long id) {
        if (!hotelRepository.existsById(id)) {
            throw new RuntimeException("Hotel not found with id: " + id);
        }
        hotelRepository.deleteById(id);
    }
}