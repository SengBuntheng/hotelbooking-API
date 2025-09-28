package com.hotelbooking.service.impl;


import com.hotelbooking.Enum.RoomStatus;
import com.hotelbooking.Repository.RoomRepository;
import com.hotelbooking.Repository.RoomTypeRepository;
import com.hotelbooking.dto.RoomDto;
import com.hotelbooking.model.Room;
import com.hotelbooking.model.RoomType;
import com.hotelbooking.service.RoomService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final ModelMapper modelMapper;

    public RoomServiceImpl(RoomRepository roomRepository,
                           RoomTypeRepository roomTypeRepository,
                           ModelMapper modelMapper) {
        this.roomRepository = roomRepository;
        this.roomTypeRepository = roomTypeRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public RoomDto createRoom(RoomDto roomDto) {
        if (roomDto.getRoomTypeId() == null) {
            throw new IllegalArgumentException("Room Type ID cannot be null");
        }

        RoomType roomType = roomTypeRepository.findById(roomDto.getRoomTypeId())
                .orElseThrow(() -> new RuntimeException("RoomType not found with id: " + roomDto.getRoomTypeId()));

        Room room = modelMapper.map(roomDto, Room.class);
        room.setRoomType(roomType);
        room.setId(null); // Ensure new entity

        Room savedRoom = roomRepository.save(room);
        RoomDto result = modelMapper.map(savedRoom, RoomDto.class);
        result.setRoomTypeId(roomType.getId());
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public RoomDto getRoomById(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + id));
        RoomDto result = modelMapper.map(room, RoomDto.class);
        if (room.getRoomType() != null) {
            result.setRoomTypeId(room.getRoomType().getId());
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomDto> getAllRooms() {
        return roomRepository.findAll().stream()
                .map(room -> {
                    RoomDto dto = modelMapper.map(room, RoomDto.class);
                    if (room.getRoomType() != null) {
                        dto.setRoomTypeId(room.getRoomType().getId());
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public RoomDto updateRoom(Long id, RoomDto roomDto) {
        Room existingRoom = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + id));

        if (roomDto.getRoomTypeId() == null) {
            throw new IllegalArgumentException("Room Type ID cannot be null");
        }

        RoomType roomType = roomTypeRepository.findById(roomDto.getRoomTypeId())
                .orElseThrow(() -> new RuntimeException("RoomType not found with id: " + roomDto.getRoomTypeId()));

        // Update only non-null fields
        if (roomDto.getRoomNumber() != null) {
            existingRoom.setRoomNumber(roomDto.getRoomNumber());
        }
        if (roomDto.getStatus() != null) {
            existingRoom.setStatus(RoomStatus.valueOf(roomDto.getStatus()));
        }
        existingRoom.setRoomType(roomType);

        Room updatedRoom = roomRepository.save(existingRoom);
        RoomDto result = modelMapper.map(updatedRoom, RoomDto.class);
        result.setRoomTypeId(roomType.getId());
        return result;
    }

    @Override
    public void deleteRoom(Long id) {
        if (!roomRepository.existsById(id)) {
            throw new RuntimeException("Room not found with id: " + id);
        }
        roomRepository.deleteById(id);
    }
}
