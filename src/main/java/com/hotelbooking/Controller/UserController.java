package com.hotelbooking.Controller;

import com.hotelbooking.dto.UserRequest;
import com.hotelbooking.dto.UserRespone;
import com.hotelbooking.model.User;
import com.hotelbooking.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // <-- IMPORT THIS
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')") // <-- ADD THIS ANNOTATION
    public ResponseEntity<List<UserRespone>> getAllUsers() {
        return ResponseEntity.ok(userService.findAll());
    }

    // ... (rest of the controller methods)
    @GetMapping("/{id}")
    public ResponseEntity<UserRespone> getUserById(@PathVariable Long id) {
        UserRespone user = userService.findbyid(id);
        if (user != null) {
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.notFound().build();
    }


    @PutMapping("/{id}")
    public ResponseEntity<UserRespone> updateUser(@PathVariable Long id, @RequestBody UserRequest userRequest) {

        userService.Update(id ,userRequest);
        UserRespone updatedUser = userService.Update(id, userRequest);
        if (updatedUser != null) {
            return ResponseEntity.ok(updatedUser);
        }
        return ResponseEntity.notFound().build();
    }
}