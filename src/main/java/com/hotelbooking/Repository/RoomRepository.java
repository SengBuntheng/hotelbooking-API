package com.hotelbooking.Repository;

import com.hotelbooking.model.Hotel;
import com.hotelbooking.model.Room;
import com.hotelbooking.Enum.RoomStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {

    @Query("SELECT DISTINCT h FROM Hotel h " +
            "LEFT JOIN FETCH h.hotelImages " +
            "LEFT JOIN FETCH h.roomTypes rt " +
            "LEFT JOIN FETCH rt.rooms")
    List<Hotel> findAllWithDetails();


    List<Room> findByRoomTypeHotelId(Long hotelId);

    // ?? Change parameter type to RoomStatus
    List<Room> findByStatus(RoomStatus status);

    @Query("SELECT r FROM Room r " +
            "LEFT JOIN FETCH r.roomType rt " +
            "LEFT JOIN FETCH rt.hotel h " +
            "WHERE r.status = :status")
    List<Room> findByStatusWithDetails(@Param("status") RoomStatus status);
}
