package com.hotelbooking.service.impl;

import com.hotelbooking.Repository.HotelRepository;
import com.hotelbooking.Repository.StaffRepository;
import com.hotelbooking.dto.StaffDto;
import com.hotelbooking.model.Hotel;
import com.hotelbooking.model.Staff;
import com.hotelbooking.service.StaffService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StaffServiceImpl implements StaffService {

    private final StaffRepository staffRepository;
    private final HotelRepository hotelRepository;
    private final ModelMapper modelMapper;

    public StaffServiceImpl(StaffRepository staffRepository, HotelRepository hotelRepository, ModelMapper modelMapper) {
        this.staffRepository = staffRepository;
        this.hotelRepository = hotelRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public StaffDto createStaff(StaffDto staffDto) {
        Hotel hotel = hotelRepository.findById(staffDto.getHotelId())
                .orElseThrow(() -> new RuntimeException("Hotel not found with id: " + staffDto.getHotelId()));

        Staff staff = modelMapper.map(staffDto, Staff.class);
        staff.setHotel(hotel);

        Staff savedStaff = staffRepository.save(staff);
        return modelMapper.map(savedStaff, StaffDto.class);
    }

    @Override
    public StaffDto getStaffById(Long id) {
        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Staff not found with id: " + id));
        return modelMapper.map(staff, StaffDto.class);
    }

    @Override
    public List<StaffDto> getAllStaff() {
        return staffRepository.findAll().stream()
                .map(staff -> modelMapper.map(staff, StaffDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public StaffDto updateStaff(Long id, StaffDto staffDto) {
        Staff existingStaff = staffRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Staff not found with id: " + id));

        Hotel hotel = hotelRepository.findById(staffDto.getHotelId())
                .orElseThrow(() -> new RuntimeException("Hotel not found with id: " + staffDto.getHotelId()));

        existingStaff.setFirstName(staffDto.getFirstName());
        existingStaff.setLastName(staffDto.getLastName());
        existingStaff.setEmail(staffDto.getEmail());
        existingStaff.setPhone(staffDto.getPhone());
        existingStaff.setRole(staffDto.getRole());
        existingStaff.setHireDate(staffDto.getHireDate());
        existingStaff.setHotel(hotel);

        Staff updatedStaff = staffRepository.save(existingStaff);
        return modelMapper.map(updatedStaff, StaffDto.class);
    }

    @Override
    public void deleteStaff(Long id) {
        if (!staffRepository.existsById(id)) {
            throw new RuntimeException("Staff not found with id: " + id);
        }
        staffRepository.deleteById(id);
    }
}