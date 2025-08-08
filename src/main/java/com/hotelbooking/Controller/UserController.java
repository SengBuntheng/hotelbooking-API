package com.hotelbooking.Controller;

import com.hotelbooking.dto.UserRequest;
import com.hotelbooking.dto.UserRespone;
import com.hotelbooking.model.User;
import com.hotelbooking.service.UserService;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<List<UserRespone>> getAllUsers() {
        return ResponseEntity.ok(userService.findAll());
    }

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
        User user = new User();
        user.setId(id);
        user.setEmail(userRequest.getEmail());
        user.setFirstName(userRequest.getFirstName());
        user.setLastName(userRequest.getLastName());
        user.setPhone(userRequest.getPhone());

        UserRespone updatedUser = userService.Update(id, userRequest);
        if (updatedUser != null) {
            return ResponseEntity.ok(updatedUser);
        }
        return ResponseEntity.notFound().build();
    }
}