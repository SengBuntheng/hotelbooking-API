package com.hotelbooking.service;

import com.hotelbooking.dto.RoomTypeDto;
import java.util.List;

public interface RoomTypeService {
    RoomTypeDto createRoomType(RoomTypeDto roomTypeDto);
    RoomTypeDto getRoomTypeById(Long id);
    List<RoomTypeDto> getAllRoomTypes();
    RoomTypeDto updateRoomType(Long id, RoomTypeDto roomTypeDto);
    void deleteRoomType(Long id);
}