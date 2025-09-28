package com.hotelbooking.Controller;

import com.hotelbooking.dto.RoomTypeDto;
import com.hotelbooking.service.RoomTypeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v1/room-types")
public class RoomTypeController {

    private final RoomTypeService roomTypeService;

    public RoomTypeController(RoomTypeService roomTypeService) {
        this.roomTypeService = roomTypeService;
    }

    @PostMapping
    public ResponseEntity<RoomTypeDto> createRoomType(@Valid @RequestBody RoomTypeDto roomTypeDto) {
        try {
            RoomTypeDto created = roomTypeService.createRoomType(roomTypeDto);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoomTypeDto> getRoomTypeById(@PathVariable Long id) {
        try {
            RoomTypeDto roomType = roomTypeService.getRoomTypeById(id);
            return ResponseEntity.ok(roomType);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping
    public ResponseEntity<List<RoomTypeDto>> getAllRoomTypes() {
        List<RoomTypeDto> roomTypes = roomTypeService.getAllRoomTypes();
        return ResponseEntity.ok(roomTypes);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RoomTypeDto> updateRoomType(@PathVariable Long id, @Valid @RequestBody RoomTypeDto roomTypeDto) {
        try {
            RoomTypeDto updated = roomTypeService.updateRoomType(id, roomTypeDto);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoomType(@PathVariable Long id) {
        try {
            roomTypeService.deleteRoomType(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}