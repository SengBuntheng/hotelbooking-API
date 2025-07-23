package com.hotelbooking.Repository;

import com.hotelbooking.model.HotelAmenity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface HotelAmenityRepository extends JpaRepository<HotelAmenity, Long> {
}
