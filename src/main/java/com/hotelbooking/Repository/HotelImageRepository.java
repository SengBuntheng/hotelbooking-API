package com.hotelbooking.Repository;

import com.hotelbooking.model.HotelImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface HotelImageRepository extends JpaRepository<HotelImage, Long> {
}
