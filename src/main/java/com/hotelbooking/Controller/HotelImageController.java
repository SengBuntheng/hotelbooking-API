package com.hotelbooking.Controller;

import com.hotelbooking.dto.HotelImageDto;
import com.hotelbooking.service.HotelImageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v1/hotel-images")
public class HotelImageController {

    private final HotelImageService hotelImageService;

    public HotelImageController(HotelImageService hotelImageService) {
        this.hotelImageService = hotelImageService;
    }

    @PostMapping
    public ResponseEntity<HotelImageDto> createHotelImage(@Valid @RequestBody HotelImageDto hotelImageDto) {
        try {
            HotelImageDto created = hotelImageService.createHotelImage(hotelImageDto);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<HotelImageDto> getHotelImageById(@PathVariable Long id) {
        try {
            HotelImageDto hotelImage = hotelImageService.getHotelImageById(id);
            return ResponseEntity.ok(hotelImage);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping
    public ResponseEntity<List<HotelImageDto>> getAllHotelImages() {
        List<HotelImageDto> hotelImages = hotelImageService.getAllHotelImages();
        return ResponseEntity.ok(hotelImages);
    }

    @PutMapping("/{id}")
    public ResponseEntity<HotelImageDto> updateHotelImage(@PathVariable Long id, @Valid @RequestBody HotelImageDto hotelImageDto) {
        try {
            HotelImageDto updated = hotelImageService.updateHotelImage(id, hotelImageDto);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHotelImage(@PathVariable Long id) {
        try {
            hotelImageService.deleteHotelImage(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}