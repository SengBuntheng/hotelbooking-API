package com.hotelbooking.Repository;

import com.hotelbooking.model.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface RoomTypeRepository extends JpaRepository<RoomType, Long> {

    List<RoomType> findByHotelId(Long hotelId);

    @Query("SELECT rt FROM RoomType rt " +
            "LEFT JOIN FETCH rt.hotel h " +
            "WHERE rt.hotel.id = :hotelId")
    List<RoomType> findByHotelIdWithDetails(Long hotelId);
}
