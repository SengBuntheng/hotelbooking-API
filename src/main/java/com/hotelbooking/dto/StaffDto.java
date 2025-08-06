package com.hotelbooking.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class StaffDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String role;
    private LocalDate hireDate;
    private Long hotelId;
}