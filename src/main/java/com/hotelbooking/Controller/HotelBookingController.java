package com.hotelbooking.Controller;

import com.hotelbooking.dto.*;
import com.hotelbooking.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v1/hotel-booking")
@RequiredArgsConstructor
@Slf4j
public class HotelBookingController {

    private final HotelService hotelService;
    private final RoomService roomService;
    private final RoomTypeService roomTypeService;
    private final HotelImageService hotelImageService;

    // ===== HOTELS =====

    @PostMapping("/hotels")
    public ResponseEntity<ApiResponse<HotelDto>> createHotel(@Valid @RequestBody HotelDto hotelDto) {
        try {
            HotelDto created = hotelService.createHotel(hotelDto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Hotel created successfully", created));
        } catch (Exception e) {
            log.error("Failed to create hotel: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Failed to create hotel: " + e.getMessage()));
        }
    }

    @GetMapping("/hotels/{id}")
    public ResponseEntity<ApiResponse<HotelDto>> getHotelById(@PathVariable Long id) {
        try {
            HotelDto hotel = hotelService.getHotelById(id);
            return ResponseEntity.ok(ApiResponse.success("Hotel retrieved successfully", hotel));
        } catch (RuntimeException e) {
            log.warn("Hotel not found with id: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Hotel not found with id: " + id));
        }
    }

    @GetMapping("/hotels")
    public ResponseEntity<ApiResponse<List<HotelDto>>> getAllHotels() {
        try {
            List<HotelDto> hotels = hotelService.getAllHotels();
            return ResponseEntity.ok(ApiResponse.success("Hotels retrieved successfully", hotels));
        } catch (Exception e) {
            log.error("Failed to get all hotels: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve hotels"));
        }
    }

    @GetMapping("/hotels/search")
    public ResponseEntity<ApiResponse<List<HotelDto>>> searchHotels(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Double minRating) {
        try {
            List<HotelDto> hotels = hotelService.getAllHotels();

            if (city != null && !city.trim().isEmpty()) {
                hotels = hotels.stream()
                        .filter(h -> h.getCity() != null && h.getCity().toLowerCase().contains(city.toLowerCase()))
                        .toList();
            }

            if (minRating != null) {
                hotels = hotels.stream()
                        .filter(h -> h.getRating() != null && h.getRating().doubleValue() >= minRating)
                        .toList();
            }

            return ResponseEntity.ok(ApiResponse.success("Hotels found", hotels));
        } catch (Exception e) {
            log.error("Failed to search hotels: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to search hotels"));
        }
    }

    @PutMapping("/hotels/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<HotelDto>> updateHotel(@PathVariable Long id, @Valid @RequestBody HotelDto hotelDto) {
        try {
            HotelDto updated = hotelService.updateHotel(id, hotelDto);
            return ResponseEntity.ok(ApiResponse.success("Hotel updated successfully", updated));
        } catch (RuntimeException e) {
            log.warn("Failed to update hotel {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Hotel not found with id: " + id));
        }
    }

    @DeleteMapping("/hotels/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteHotel(@PathVariable Long id) {
        try {
            hotelService.deleteHotel(id);
            return ResponseEntity.ok(ApiResponse.success("Hotel deleted successfully", null));
        } catch (RuntimeException e) {
            log.warn("Failed to delete hotel {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Hotel not found with id: " + id));
        }
    }

    // ===== ROOM TYPES =====

    @PostMapping("/room-types")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RoomTypeDto>> createRoomType(@Valid @RequestBody RoomTypeDto roomTypeDto) {
        try {
            RoomTypeDto created = roomTypeService.createRoomType(roomTypeDto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Room type created successfully", created));
        } catch (Exception e) {
            log.error("Failed to create room type: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Failed to create room type: " + e.getMessage()));
        }
    }

    @GetMapping("/room-types/{id}")
    public ResponseEntity<ApiResponse<RoomTypeDto>> getRoomTypeById(@PathVariable Long id) {
        try {
            RoomTypeDto roomType = roomTypeService.getRoomTypeById(id);
            return ResponseEntity.ok(ApiResponse.success("Room type retrieved successfully", roomType));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Room type not found with id: " + id));
        }
    }

    @GetMapping("/room-types")
    public ResponseEntity<ApiResponse<List<RoomTypeDto>>> getAllRoomTypes() {
        try {
            List<RoomTypeDto> roomTypes = roomTypeService.getAllRoomTypes();
            return ResponseEntity.ok(ApiResponse.success("Room types retrieved successfully", roomTypes));
        } catch (Exception e) {
            log.error("Failed to get room types: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve room types"));
        }
    }

    @GetMapping("/room-types/by-hotel/{hotelId}")
    public ResponseEntity<ApiResponse<List<RoomTypeDto>>> getRoomTypesByHotelId(@PathVariable Long hotelId) {
        try {
            List<RoomTypeDto> roomTypes = roomTypeService.getRoomTypesByHotelId(hotelId);
            return ResponseEntity.ok(ApiResponse.success("Room types retrieved for hotel", roomTypes));
        } catch (Exception e) {
            log.error("Failed to get room types for hotel {}: {}", hotelId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve room types"));
        }
    }

    @PutMapping("/room-types/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RoomTypeDto>> updateRoomType(@PathVariable Long id, @Valid @RequestBody RoomTypeDto roomTypeDto) {
        try {
            RoomTypeDto updated = roomTypeService.updateRoomType(id, roomTypeDto);
            return ResponseEntity.ok(ApiResponse.success("Room type updated successfully", updated));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Room type not found with id: " + id));
        }
    }

    @DeleteMapping("/room-types/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteRoomType(@PathVariable Long id) {
        try {
            roomTypeService.deleteRoomType(id);
            return ResponseEntity.ok(ApiResponse.success("Room type deleted successfully", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Room type not found with id: " + id));
        }
    }

    // ===== ROOMS =====

    @PostMapping("/rooms")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RoomDto>> createRoom(@Valid @RequestBody RoomDto roomDto) {
        try {
            RoomDto created = roomService.createRoom(roomDto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Room created successfully", created));
        } catch (Exception e) {
            log.error("Failed to create room: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Failed to create room: " + e.getMessage()));
        }
    }

    @GetMapping("/rooms/{id}")
    public ResponseEntity<ApiResponse<RoomDto>> getRoomById(@PathVariable Long id) {
        try {
            RoomDto room = roomService.getRoomById(id);
            return ResponseEntity.ok(ApiResponse.success("Room retrieved with complete details", room));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Room not found with id: " + id));
        }
    }

    @GetMapping("/rooms")
    public ResponseEntity<ApiResponse<List<RoomDto>>> getAllRooms() {
        try {
            List<RoomDto> rooms = roomService.getAllRooms();
            return ResponseEntity.ok(ApiResponse.success("Rooms retrieved with complete details", rooms));
        } catch (Exception e) {
            log.error("Failed to get rooms: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve rooms"));
        }
    }

    @GetMapping("/rooms/by-hotel/{hotelId}")
    public ResponseEntity<ApiResponse<List<RoomDto>>> getRoomsByHotelId(@PathVariable Long hotelId) {
        try {
            List<RoomDto> rooms = roomService.getRoomsByHotelId(hotelId);
            return ResponseEntity.ok(ApiResponse.success("Rooms retrieved for hotel", rooms));
        } catch (Exception e) {
            log.error("Failed to get rooms for hotel {}: {}", hotelId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("No rooms found for hotel ID: " + hotelId));
        }
    }

    @GetMapping("/rooms/by-status/{status}")
    public ResponseEntity<ApiResponse<List<RoomDto>>> getRoomsByStatus(@PathVariable String status) {
        try {
            List<RoomDto> rooms = roomService.getRoomsByStatus(status);
            return ResponseEntity.ok(ApiResponse.success("Rooms retrieved by status", rooms));
        } catch (Exception e) {
            log.error("Failed to get rooms by status {}: {}", status, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Invalid room status: " + status));
        }
    }

    @GetMapping("/rooms/available/{hotelId}")
    public ResponseEntity<ApiResponse<List<RoomDto>>> getAvailableRoomsByHotel(@PathVariable Long hotelId) {
        try {
            List<RoomDto> rooms = roomService.getAvailableRoomsByHotelId(hotelId);
            return ResponseEntity.ok(ApiResponse.success("Available rooms retrieved", rooms));
        } catch (Exception e) {
            log.error("Failed to get available rooms for hotel {}: {}", hotelId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve available rooms"));
        }
    }

    @PutMapping("/rooms/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RoomDto>> updateRoom(@PathVariable Long id, @Valid @RequestBody RoomDto roomDto) {
        try {
            RoomDto updated = roomService.updateRoom(id, roomDto);
            return ResponseEntity.ok(ApiResponse.success("Room updated successfully", updated));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Room not found with id: " + id));
        }
    }

    @DeleteMapping("/rooms/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteRoom(@PathVariable Long id) {
        try {
            roomService.deleteRoom(id);
            return ResponseEntity.ok(ApiResponse.success("Room deleted successfully", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Room not found with id: " + id));
        }
    }

    // ===== HOTEL IMAGES =====

    @PostMapping("/hotel-images")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<HotelImageDto>> createHotelImage(@Valid @RequestBody HotelImageDto hotelImageDto) {
        try {
            HotelImageDto created = hotelImageService.createHotelImage(hotelImageDto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Hotel image created successfully", created));
        } catch (Exception e) {
            log.error("Failed to create hotel image: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Failed to create hotel image: " + e.getMessage()));
        }
    }

    @GetMapping("/hotel-images/{id}")
    public ResponseEntity<ApiResponse<HotelImageDto>> getHotelImageById(@PathVariable Long id) {
        try {
            HotelImageDto hotelImage = hotelImageService.getHotelImageById(id);
            return ResponseEntity.ok(ApiResponse.success("Hotel image retrieved successfully", hotelImage));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Hotel image not found with id: " + id));
        }
    }

    @GetMapping("/hotel-images")
    public ResponseEntity<ApiResponse<List<HotelImageDto>>> getAllHotelImages() {
        try {
            List<HotelImageDto> hotelImages = hotelImageService.getAllHotelImages();
            return ResponseEntity.ok(ApiResponse.success("Hotel images retrieved successfully", hotelImages));
        } catch (Exception e) {
            log.error("Failed to get hotel images: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve hotel images"));
        }
    }

    @GetMapping("/hotel-images/by-hotel/{hotelId}")
    public ResponseEntity<ApiResponse<List<HotelImageDto>>> getHotelImagesByHotelId(@PathVariable Long hotelId) {
        try {
            List<HotelImageDto> hotelImages = hotelImageService.getHotelImagesByHotelId(hotelId);
            return ResponseEntity.ok(ApiResponse.success("Hotel images retrieved for hotel", hotelImages));
        } catch (Exception e) {
            log.error("Failed to get images for hotel {}: {}", hotelId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("No images found for hotel ID: " + hotelId));
        }
    }

    @PutMapping("/hotel-images/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<HotelImageDto>> updateHotelImage(@PathVariable Long id, @Valid @RequestBody HotelImageDto hotelImageDto) {
        try {
            HotelImageDto updated = hotelImageService.updateHotelImage(id, hotelImageDto);
            return ResponseEntity.ok(ApiResponse.success("Hotel image updated successfully", updated));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Hotel image not found with id: " + id));
        }
    }

    @DeleteMapping("/hotel-images/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteHotelImage(@PathVariable Long id) {
        try {
            hotelImageService.deleteHotelImage(id);
            return ResponseEntity.ok(ApiResponse.success("Hotel image deleted successfully", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Hotel image not found with id: " + id));
        }
    }

    // ===== DASHBOARD & STATISTICS =====

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboard() {
        try {
            Map<String, Object> dashboard = new HashMap<>();

            List<HotelDto> hotels = hotelService.getAllHotels();
            List<RoomDto> rooms = roomService.getAllRooms();
            List<RoomTypeDto> roomTypes = roomTypeService.getAllRoomTypes();
            List<HotelImageDto> hotelImages = hotelImageService.getAllHotelImages();

            // Basic counts
            dashboard.put("totalHotels", hotels.size());
            dashboard.put("totalRooms", rooms.size());
            dashboard.put("totalRoomTypes", roomTypes.size());
            dashboard.put("totalHotelImages", hotelImages.size());

            // Room status distribution
            Map<String, Long> statusCounts = rooms.stream()
                    .collect(java.util.stream.Collectors.groupingBy(
                            RoomDto::getStatus,
                            java.util.stream.Collectors.counting()
                    ));
            dashboard.put("roomStatusDistribution", statusCounts);

            // Available rooms count
            long availableRooms = rooms.stream()
                    .filter(r -> "AVAILABLE".equalsIgnoreCase(r.getStatus()))
                    .count();
            dashboard.put("availableRooms", availableRooms);

            // Recent data
            dashboard.put("recentHotels", hotels.size() > 5 ? hotels.subList(0, 5) : hotels);
            dashboard.put("recentRooms", rooms.size() > 5 ? rooms.subList(0, 5) : rooms);

            return ResponseEntity.ok(ApiResponse.success("Dashboard data retrieved successfully", dashboard));
        } catch (Exception e) {
            log.error("Failed to get dashboard data: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve dashboard data"));
        }
    }
}