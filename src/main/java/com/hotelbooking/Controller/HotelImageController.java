package com.hotelbooking.Controller;

import com.hotelbooking.dto.HotelImageDto;
import com.hotelbooking.service.HotelImageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/v1/hotel-images")
public class HotelImageController {

    private final HotelImageService hotelImageService;

    public HotelImageController(HotelImageService hotelImageService) {
        this.hotelImageService = hotelImageService;
    }

    @PostMapping()
    public ResponseEntity<HotelImageDto> createHotelImage(@RequestBody HotelImageDto hotelImageDto) {
        return new ResponseEntity<>(hotelImageService.createHotelImage(hotelImageDto), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<HotelImageDto> getHotelImageById(@PathVariable Long id) {
        return ResponseEntity.ok(hotelImageService.getHotelImageById(id));
    }

    @GetMapping
    public ResponseEntity<List<HotelImageDto>> getAllHotelImages() {
        return ResponseEntity.ok(hotelImageService.getAllHotelImages());
    }

    @PutMapping("/{id}")
    public ResponseEntity<HotelImageDto> updateHotelImage(@PathVariable Long id, @RequestBody HotelImageDto hotelImageDto) {
        return ResponseEntity.ok(hotelImageService.updateHotelImage(id, hotelImageDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHotelImage(@PathVariable Long id) {
        hotelImageService.deleteHotelImage(id);
        return ResponseEntity.noContent().build();
    }
}