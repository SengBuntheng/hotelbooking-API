package com.hotelbooking.Controller;

import com.hotelbooking.dto.LoginRequest;
import com.hotelbooking.dto.LoginResponse;
import com.hotelbooking.dto.UserRequest;
import com.hotelbooking.dto.UserRespone;
import com.hotelbooking.model.User;
import com.hotelbooking.service.AuthService;
import com.hotelbooking.service.UserService;
import jakarta.validation.constraints.Email;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.PublicKey;

@RestController
@RequestMapping("/api/v1/auth")
@Slf4j
public class AuthController {


    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

//    @PostMapping("/register")
//    public ResponseEntity<UserRespone> register(@RequestBody UserRequest userRequest) {
//        log.info("Registering user: {}", userRequest.getEmail());
//        UserRespone createdUser = userService.Create(userRequest);
//        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
//    }


}
