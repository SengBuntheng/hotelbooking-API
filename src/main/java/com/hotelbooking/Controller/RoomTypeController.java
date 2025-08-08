package com.hotelbooking.Controller;

import com.hotelbooking.dto.RoomTypeDto;
import com.hotelbooking.service.RoomTypeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/v1/room-types")
public class RoomTypeController {

    private final RoomTypeService roomTypeService;

    public RoomTypeController(RoomTypeService roomTypeService) {
        this.roomTypeService = roomTypeService;
    }

    @PostMapping
    public ResponseEntity<RoomTypeDto> createRoomType(@RequestBody RoomTypeDto roomTypeDto) {
        return new ResponseEntity<>(roomTypeService.createRoomType(roomTypeDto), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoomTypeDto> getRoomTypeById(@PathVariable Long id) {
        return ResponseEntity.ok(roomTypeService.getRoomTypeById(id));
    }

    @GetMapping
    public ResponseEntity<List<RoomTypeDto>> getAllRoomTypes() {
        return ResponseEntity.ok(roomTypeService.getAllRoomTypes());
    }

    @PutMapping("/{id}")
    public ResponseEntity<RoomTypeDto> updateRoomType(@PathVariable Long id, @RequestBody RoomTypeDto roomTypeDto) {
        return ResponseEntity.ok(roomTypeService.updateRoomType(id, roomTypeDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoomType(@PathVariable Long id) {
        roomTypeService.deleteRoomType(id);
        return ResponseEntity.noContent().build();
    }
}