package com.hotelbooking.Repository;

import com.hotelbooking.Enum.RoomStatus;
import com.hotelbooking.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {

    List<Room> findByRoomTypeHotelId(Long hotelId);

    List<Room> findByStatus(RoomStatus status);

    List<Room> findByRoomTypeId(Long roomTypeId);

    @Query("SELECT r FROM Room r WHERE r.roomType.hotel.id = :hotelId AND r.status = :status")
    List<Room> findByHotelIdAndStatus(@Param("hotelId") Long hotelId, @Param("status") RoomStatus status);

    @Query("SELECT COUNT(r) FROM Room r WHERE r.roomType.hotel.id = :hotelId")
    Long countRoomsByHotelId(@Param("hotelId") Long hotelId);

    boolean existsByRoomNumberAndRoomTypeHotelId(String roomNumber, Long hotelId);
}