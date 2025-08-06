package com.hotelbooking.service;

import com.hotelbooking.dto.StaffDto;
import java.util.List;

public interface StaffService {
    StaffDto createStaff(StaffDto staffDto);
    StaffDto getStaffById(Long id);
    List<StaffDto> getAllStaff();
    StaffDto updateStaff(Long id, StaffDto staffDto);
    void deleteStaff(Long id);
}