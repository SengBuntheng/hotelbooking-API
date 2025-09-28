package com.hotelbooking.Repository;

import com.hotelbooking.dto.HotelImageDto;
import com.hotelbooking.model.HotelImage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HotelImageRepository extends JpaRepository<HotelImage, Long> {

    List<HotelImage> findByHotelId(Long hotelId);

    List<HotelImage> findByHotelIdAndIsPrimary(Long hotelId, Boolean isPrimary);

    List<HotelImage> findByImageType(String imageType);
    List <HotelImageDto> getHotelImagesByHotelId(Long hotelId);
}
