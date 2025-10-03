package com.hotelbooking.service.impl;

import com.hotelbooking.Enum.RoomStatus;
import com.hotelbooking.Repository.HotelImageRepository;
import com.hotelbooking.Repository.HotelRepository;
import com.hotelbooking.Repository.RoomRepository;
import com.hotelbooking.Repository.RoomTypeRepository;
import com.hotelbooking.dto.RoomDto;
import com.hotelbooking.dto.RoomTypeDto;
import com.hotelbooking.dto.HotelDto;
import com.hotelbooking.dto.HotelImageDto;
import com.hotelbooking.model.Room;
import com.hotelbooking.model.RoomType;
import com.hotelbooking.model.Hotel;
import com.hotelbooking.model.HotelImage;
import com.hotelbooking.service.RoomService;
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
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final HotelRepository hotelRepository;
    private final HotelImageRepository hotelImageRepository;

    @Override
    public RoomDto createRoom(RoomDto roomDto) {
        try {
            validateRoomDto(roomDto);

            // Check if room number already exists in the same hotel
            RoomType roomType = roomTypeRepository.findById(roomDto.getRoomTypeId())
                    .orElseThrow(() -> new RuntimeException("Room type not found with id: " + roomDto.getRoomTypeId()));

            boolean roomExists = roomRepository.existsByRoomNumberAndRoomTypeHotelId(
                    roomDto.getRoomNumber(), roomType.getHotel().getId());

            if (roomExists) {
                throw new RuntimeException("Room number " + roomDto.getRoomNumber() + " already exists in this hotel");
            }

            Room room = new Room();
            room.setRoomNumber(roomDto.getRoomNumber());
            room.setStatus(RoomStatus.valueOf(roomDto.getStatus()));
            room.setRoomType(roomType);
            room.setCreatedAt(LocalDateTime.now());

            Room savedRoom = roomRepository.save(room);
            log.info("Room created successfully with ID: {}", savedRoom.getId());

            return mapToDto(savedRoom, true);
        } catch (Exception e) {
            log.error("Failed to create room: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create room: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public RoomDto getRoomById(Long id) {
        try {
            Room room = roomRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Room not found with id: " + id));
            return mapToDto(room, true);
        } catch (Exception e) {
            log.error("Failed to get room by id {}: {}", id, e.getMessage());
            throw new RuntimeException("Failed to retrieve room: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomDto> getAllRooms() {
        try {
            List<Room> rooms = roomRepository.findAll();
            return rooms.stream()
                    .map(room -> mapToDto(room, true))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to get all rooms: {}", e.getMessage());
            throw new RuntimeException("Failed to retrieve rooms: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomDto> getRoomsByHotelId(Long hotelId) {
        try {
            if (hotelId == null) {
                throw new IllegalArgumentException("Hotel ID cannot be null");
            }

            List<Room> rooms = roomRepository.findByRoomTypeHotelId(hotelId);
            return rooms.stream()
                    .map(room -> mapToDto(room, true))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to get rooms for hotel {}: {}", hotelId, e.getMessage());
            throw new RuntimeException("Failed to retrieve rooms: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomDto> getRoomsByStatus(String status) {
        try {
            if (status == null || status.trim().isEmpty()) {
                throw new IllegalArgumentException("Room status cannot be null or empty");
            }

            RoomStatus roomStatus = RoomStatus.valueOf(status.toUpperCase());
            List<Room> rooms = roomRepository.findByStatus(roomStatus);
            return rooms.stream()
                    .map(room -> mapToDto(room, true))
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            log.error("Invalid room status: {}", status);
            throw new RuntimeException("Invalid room status: " + status);
        } catch (Exception e) {
            log.error("Failed to get rooms by status {}: {}", status, e.getMessage());
            throw new RuntimeException("Failed to retrieve rooms: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomDto> getAvailableRoomsByHotelId(Long hotelId) {
        try {
            if (hotelId == null) {
                throw new IllegalArgumentException("Hotel ID cannot be null");
            }

            List<Room> rooms = roomRepository.findByHotelIdAndStatus(hotelId, RoomStatus.AVAILABLE);
            return rooms.stream()
                    .map(room -> mapToDto(room, true))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to get available rooms for hotel {}: {}", hotelId, e.getMessage());
            throw new RuntimeException("Failed to retrieve available rooms: " + e.getMessage());
        }
    }

    @Override
    public RoomDto updateRoom(Long id, RoomDto roomDto) {
        try {
            Room existingRoom = roomRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Room not found with id: " + id));

            // Update fields if provided
            if (roomDto.getRoomNumber() != null && !roomDto.getRoomNumber().trim().isEmpty()) {
                String newRoomNumber = roomDto.getRoomNumber().trim();

                // Check if room number already exists in the same hotel (excluding current room)
                if (!newRoomNumber.equals(existingRoom.getRoomNumber())) {
                    Long hotelId = existingRoom.getRoomType().getHotel().getId();
                    boolean roomExists = roomRepository.existsByRoomNumberAndRoomTypeHotelId(newRoomNumber, hotelId);

                    if (roomExists) {
                        throw new RuntimeException("Room number " + newRoomNumber + " already exists in this hotel");
                    }
                }

                existingRoom.setRoomNumber(newRoomNumber);
            }

            if (roomDto.getStatus() != null && !roomDto.getStatus().trim().isEmpty()) {
                existingRoom.setStatus(RoomStatus.valueOf(roomDto.getStatus().toUpperCase()));
            }

            if (roomDto.getRoomTypeId() != null) {
                RoomType roomType = roomTypeRepository.findById(roomDto.getRoomTypeId())
                        .orElseThrow(() -> new RuntimeException("Room type not found with id: " + roomDto.getRoomTypeId()));
                existingRoom.setRoomType(roomType);
            }

            existingRoom.setUpdatedAt(LocalDateTime.now());

            Room updatedRoom = roomRepository.save(existingRoom);
            log.info("Room updated successfully with ID: {}", updatedRoom.getId());

            return mapToDto(updatedRoom, true);
        } catch (Exception e) {
            log.error("Failed to update room {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to update room: " + e.getMessage());
        }
    }

    @Override
    public void deleteRoom(Long id) {
        try {
            if (!roomRepository.existsById(id)) {
                throw new RuntimeException("Room not found with id: " + id);
            }
            roomRepository.deleteById(id);
            log.info("Room deleted successfully with ID: {}", id);
        } catch (Exception e) {
            log.error("Failed to delete room {}: {}", id, e.getMessage());
            throw new RuntimeException("Failed to delete room: " + e.getMessage());
        }
    }

    // Helper methods
    private RoomDto mapToDto(Room room, boolean includeNestedObjects) {
        RoomDto dto = new RoomDto();
        dto.setId(room.getId());
        dto.setRoomNumber(room.getRoomNumber());
        dto.setStatus(room.getStatus().toString());

        if (room.getRoomType() != null) {
            dto.setRoomTypeId(room.getRoomType().getId());

            if (includeNestedObjects) {
                // Map room type
                RoomTypeDto roomTypeDto = mapRoomTypeToDto(room.getRoomType());
                dto.setRoomType(roomTypeDto);

                // Map hotel if available
                if (room.getRoomType().getHotel() != null) {
                    HotelDto hotelDto = mapHotelToDto(room.getRoomType().getHotel());
                    dto.setHotel(hotelDto);
                    roomTypeDto.setHotel(hotelDto);

                    // Load hotel images
                    List<HotelImage> hotelImages = hotelImageRepository.findByHotelId(hotelDto.getId());
                    List<HotelImageDto> hotelImageDtos = hotelImages.stream()
                            .map(this::mapHotelImageToDto)
                            .collect(Collectors.toList());
                    dto.setHotelImages(hotelImageDtos);
                    hotelDto.setHotelImages(hotelImageDtos);
                }
            }
        }

        return dto;
    }

    private RoomTypeDto mapRoomTypeToDto(RoomType roomType) {
        RoomTypeDto dto = new RoomTypeDto();
        dto.setId(roomType.getId());
        dto.setTypeName(roomType.getTypeName());
        dto.setDescription(roomType.getDescription());
        dto.setPricePerNight(roomType.getPricePerNight());
        dto.setMaxOccupancy(roomType.getMaxOccupancy());

        if (roomType.getHotel() != null) {
            dto.setHotelId(roomType.getHotel().getId());
        }

        return dto;
    }

    private HotelDto mapHotelToDto(Hotel hotel) {
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
        return dto;
    }

    private HotelImageDto mapHotelImageToDto(HotelImage hotelImage) {
        HotelImageDto dto = new HotelImageDto();
        dto.setId(hotelImage.getId());
        dto.setImageUrl(hotelImage.getImageUrl());
        dto.setImageType(hotelImage.getImageType());
        dto.setAltText(hotelImage.getAltText());
        dto.setIsPrimary(hotelImage.getIsPrimary());

        if (hotelImage.getHotel() != null) {
            dto.setHotelId(hotelImage.getHotel().getId());
        }

        return dto;
    }

    private void validateRoomDto(RoomDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Room data cannot be null");
        }
        if (dto.getRoomNumber() == null || dto.getRoomNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Room number cannot be null or empty");
        }
        if (dto.getStatus() == null || dto.getStatus().trim().isEmpty()) {
            throw new IllegalArgumentException("Room status cannot be null or empty");
        }
        if (dto.getRoomTypeId() == null) {
            throw new IllegalArgumentException("Room type ID cannot be null");
        }

        // Validate status enum
        try {
            RoomStatus.valueOf(dto.getStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid room status: " + dto.getStatus());
        }
    }
}