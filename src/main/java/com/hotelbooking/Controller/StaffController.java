package com.hotelbooking.Controller;

import com.hotelbooking.dto.StaffDto;
import com.hotelbooking.service.StaffService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/staff")
public class StaffController {

    private final StaffService staffService;

    public StaffController(StaffService staffService) {
        this.staffService = staffService;
    }

    @PostMapping
    public ResponseEntity<StaffDto> createStaff(@RequestBody StaffDto staffDto) {
        return new ResponseEntity<>(staffService.createStaff(staffDto), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StaffDto> getStaffById(@PathVariable Long id) {
        return ResponseEntity.ok(staffService.getStaffById(id));
    }

    @GetMapping
    public ResponseEntity<List<StaffDto>> getAllStaff() {
        return ResponseEntity.ok(staffService.getAllStaff());
    }

    @PutMapping("/{id}")
    public ResponseEntity<StaffDto> updateStaff(@PathVariable Long id, @RequestBody StaffDto staffDto) {
        return ResponseEntity.ok(staffService.updateStaff(id, staffDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStaff(@PathVariable Long id) {
        staffService.deleteStaff(id);
        return ResponseEntity.noContent().build();
    }
}