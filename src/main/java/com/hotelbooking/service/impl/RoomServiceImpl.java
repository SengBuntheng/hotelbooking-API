package com.hotelbooking.service.impl;

import com.hotelbooking.Repository.RoomRepository;
import com.hotelbooking.Repository.RoomTypeRepository;
import com.hotelbooking.dto.RoomDto;
import com.hotelbooking.model.Room;
import com.hotelbooking.model.RoomType;
import com.hotelbooking.service.RoomService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final ModelMapper modelMapper;

    public RoomServiceImpl(RoomRepository roomRepository, RoomTypeRepository roomTypeRepository, ModelMapper modelMapper) {
        this.roomRepository = roomRepository;
        this.roomTypeRepository = roomTypeRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public RoomDto createRoom(RoomDto roomDto) {
        RoomType roomType = roomTypeRepository.findById(roomDto.getRoomTypeId())
                .orElseThrow(() -> new RuntimeException("RoomType not found with id: " + roomDto.getRoomTypeId()));

        Room room = modelMapper.map(roomDto, Room.class);
        room.setRoomType(roomType);

        Room savedRoom = roomRepository.save(room);
        return modelMapper.map(savedRoom, RoomDto.class);
    }

    @Override
    public RoomDto getRoomById(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + id));
        return modelMapper.map(room, RoomDto.class);
    }

    @Override
    public List<RoomDto> getAllRooms() {
        return roomRepository.findAll().stream()
                .map(room -> modelMapper.map(room, RoomDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public RoomDto updateRoom(Long id, RoomDto roomDto) {
        Room existingRoom = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + id));

        RoomType roomType = roomTypeRepository.findById(roomDto.getRoomTypeId())
                .orElseThrow(() -> new RuntimeException("RoomType not found with id: " + roomDto.getRoomTypeId()));

        existingRoom.setRoomNumber(roomDto.getRoomNumber());
        existingRoom.setStatus(roomDto.getStatus());
        existingRoom.setRoomType(roomType);

        Room updatedRoom = roomRepository.save(existingRoom);
        return modelMapper.map(updatedRoom, RoomDto.class);
    }

    @Override
    public void deleteRoom(Long id) {
        if (!roomRepository.existsById(id)) {
            throw new RuntimeException("Room not found with id: " + id);
        }
        roomRepository.deleteById(id);
    }
}