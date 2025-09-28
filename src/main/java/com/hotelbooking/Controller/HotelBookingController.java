package com.hotelbooking.Controller;

import com.hotelbooking.dto.*;
import com.hotelbooking.service.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v1/hotel-booking")
public class HotelBookingController {

    private final HotelService hotelService;
    private final RoomService roomService;
    private final RoomTypeService roomTypeService;
    private final HotelImageService hotelImageService;

    public HotelBookingController(HotelService hotelService,
                                  RoomService roomService,
                                  RoomTypeService roomTypeService,
                                  HotelImageService hotelImageService) {
        this.hotelService = hotelService;
        this.roomService = roomService;
        this.roomTypeService = roomTypeService;
        this.hotelImageService = hotelImageService;
    }

    // ===== HOTELS =====

    @PostMapping("/hotels")
    public ResponseEntity<ApiResponse<HotelDto>> createHotel(@Valid @RequestBody HotelDto hotelDto) {
        try {
            HotelDto created = hotelService.createHotel(hotelDto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Hotel created successfully", created));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Failed to create hotel: " + e.getMessage()));
        }
    }

    @GetMapping("/hotels/{id}")
    public ResponseEntity<ApiResponse<HotelDto>> getHotelById(@PathVariable Long id) {
        try {
            HotelDto hotel = hotelService.getHotelById(id);
            return ResponseEntity.ok(ApiResponse.success("Hotel found", hotel));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Hotel not found with id: " + id));
        }
    }

    @GetMapping("/hotels")
    public ResponseEntity<ApiResponse<List<HotelDto>>> getAllHotels() {
        List<HotelDto> hotels = hotelService.getAllHotels();
        return ResponseEntity.ok(ApiResponse.success("Hotels retrieved successfully", hotels));
    }

    @PutMapping("/hotels/{id}")
    public ResponseEntity<ApiResponse<HotelDto>> updateHotel(@PathVariable Long id, @Valid @RequestBody HotelDto hotelDto) {
        try {
            HotelDto updated = hotelService.updateHotel(id, hotelDto);
            return ResponseEntity.ok(ApiResponse.success("Hotel updated successfully", updated));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Hotel not found with id: " + id));
        }
    }

    @DeleteMapping("/hotels/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteHotel(@PathVariable Long id) {
        try {
            hotelService.deleteHotel(id);
            return ResponseEntity.ok(ApiResponse.success("Hotel deleted successfully", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Hotel not found with id: " + id));
        }
    }

    // ===== ROOM TYPES =====

    @PostMapping("/room-types")
    public ResponseEntity<ApiResponse<RoomTypeDto>> createRoomType(@Valid @RequestBody RoomTypeDto roomTypeDto) {
        try {
            RoomTypeDto created = roomTypeService.createRoomType(roomTypeDto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Room type created successfully", created));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Failed to create room type: " + e.getMessage()));
        }
    }

    @GetMapping("/room-types/{id}")
    public ResponseEntity<ApiResponse<RoomTypeDto>> getRoomTypeById(@PathVariable Long id) {
        try {
            RoomTypeDto roomType = roomTypeService.getRoomTypeById(id);
            return ResponseEntity.ok(ApiResponse.success("Room type found", roomType));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Room type not found with id: " + id));
        }
    }

    @GetMapping("/room-types")
    public ResponseEntity<ApiResponse<List<RoomTypeDto>>> getAllRoomTypes() {
        List<RoomTypeDto> roomTypes = roomTypeService.getAllRoomTypes();
        return ResponseEntity.ok(ApiResponse.success("Room types retrieved successfully", roomTypes));
    }

    @PutMapping("/room-types/{id}")
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
    public ResponseEntity<ApiResponse<Void>> deleteRoomType(@PathVariable Long id) {
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
    public ResponseEntity<ApiResponse<RoomDto>> createRoom(@Valid @RequestBody RoomDto roomDto) {
        try {
            RoomDto created = roomService.createRoom(roomDto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Room created successfully", created));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Failed to create room: " + e.getMessage()));
        }
    }

    @GetMapping("/rooms/{id}")
    public ResponseEntity<ApiResponse<RoomDto>> getRoomById(@PathVariable Long id) {
        try {
            RoomDto room = roomService.getRoomById(id);
            return ResponseEntity.ok(ApiResponse.success("Room found", room));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Room not found with id: " + id));
        }
    }

    @GetMapping("/rooms")
    public ResponseEntity<ApiResponse<List<RoomDto>>> getAllRooms() {
        List<RoomDto> rooms = roomService.getAllRooms();
        return ResponseEntity.ok(ApiResponse.success("Rooms retrieved successfully", rooms));
    }

    @PutMapping("/rooms/{id}")
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
    public ResponseEntity<ApiResponse<Void>> deleteRoom(@PathVariable Long id) {
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
    public ResponseEntity<ApiResponse<HotelImageDto>> createHotelImage(@Valid @RequestBody HotelImageDto hotelImageDto) {
        try {
            HotelImageDto created = hotelImageService.createHotelImage(hotelImageDto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Hotel image created successfully", created));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Failed to create hotel image: " + e.getMessage()));
        }
    }

    @GetMapping("/hotel-images/{id}")
    public ResponseEntity<ApiResponse<HotelImageDto>> getHotelImageById(@PathVariable Long id) {
        try {
            HotelImageDto hotelImage = hotelImageService.getHotelImageById(id);
            return ResponseEntity.ok(ApiResponse.success("Hotel image found", hotelImage));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Hotel image not found with id: " + id));
        }
    }

    @GetMapping("/hotel-images")
    public ResponseEntity<ApiResponse<List<HotelImageDto>>> getAllHotelImages() {
        List<HotelImageDto> hotelImages = hotelImageService.getAllHotelImages();
        return ResponseEntity.ok(ApiResponse.success("Hotel images retrieved successfully", hotelImages));
    }

    @PutMapping("/hotel-images/{id}")
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
    public ResponseEntity<ApiResponse<Void>> deleteHotelImage(@PathVariable Long id) {
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
        Map<String, Object> dashboard = new HashMap<>();

        List<HotelDto> hotels = hotelService.getAllHotels();
        List<RoomDto> rooms = roomService.getAllRooms();
        List<RoomTypeDto> roomTypes = roomTypeService.getAllRoomTypes();
        List<HotelImageDto> hotelImages = hotelImageService.getAllHotelImages();

        dashboard.put("totalHotels", hotels.size());
        dashboard.put("totalRooms", rooms.size());
        dashboard.put("totalRoomTypes", roomTypes.size());
        dashboard.put("totalHotelImages", hotelImages.size());
        dashboard.put("hotels", hotels);
        dashboard.put("recentRooms", rooms.size() > 5 ? rooms.subList(0, 5) : rooms);

        return ResponseEntity.ok(ApiResponse.success("Dashboard data retrieved successfully", dashboard));
    }
}