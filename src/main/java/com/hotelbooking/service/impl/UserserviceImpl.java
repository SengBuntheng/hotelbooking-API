package com.hotelbooking.service.impl;

import com.hotelbooking.Config.JwtUtils;
import com.hotelbooking.GlobalException.GlobalExceptionHandler;
import com.hotelbooking.Repository.UserRepository;
import com.hotelbooking.dto.UserRequest;
import com.hotelbooking.dto.UserRespone;
import com.hotelbooking.model.User;
import com.hotelbooking.service.UserService;
import com.hotelbooking.service.handler.UserHandlerService;
import io.jsonwebtoken.Jwt;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserserviceImpl implements UserService  {

    @Autowired
    UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserHandlerService userHandlerService;
    private final ModelMapper modelMapper;
    private final JwtUtils jwtUtils;
    @Autowired

    public UserserviceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           UserHandlerService userHandlerService,
                           ModelMapper modelMapper,
                           JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userHandlerService = userHandlerService;
        this.modelMapper = modelMapper;
        this.jwtUtils = jwtUtils;
    }

    @Transactional
    @Override
    public UserRespone Create(UserRequest userRequest) {

        boolean exists = userRepository.findByEmail(userRequest.getEmail()).isPresent();
        log.debug("Checking if email exists in DB: {} -> {}", userRequest.getEmail(), exists);
        if (exists) {
            throw new GlobalExceptionHandler.DuplicateResourceException("An account with this email already exists.");
        }
        // Validate userRequest (as you already do)
        userHandlerService.validateUserRequest(userRequest);

        User user = new User();
        user.setEmail(userRequest.getEmail());
        user.setPasswordHash(passwordEncoder.encode(userRequest.getPassword()));
        user.setPhone(userRequest.getPhone());
        user.setFirstName(userRequest.getFirstName());
        user.setLastName(userRequest.getLastName());

        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(new Date());
        user.setCreatedBy("system");
        user.setUpdatedBy("system");

        String token = jwtUtils.generateToken(user.getEmail());
        user.setToken(token);

        User savedUser = userRepository.save(user);
        log.info("Created user: {}", savedUser.getEmail());

        UserRespone response = modelMapper.map(savedUser, UserRespone.class);
        response.setToken(token);

        return response;
    }



    @Override
    public UserRespone Update(User user) {
        Optional<User> existingUser = userRepository.findById(user.getId());
        if (existingUser.isPresent()) {
            User updatedUser = userRepository.save(user);
            return modelMapper.map(updatedUser, UserRespone.class);
        }
        log.warn("User with ID {} not found for update", user.getId());
        return null;
    }

    @Override
    public UserRespone Delete(User user) {
        return null;
    }

    @Override
    public UserRespone findbyid(Long id) {
        return null;
    }

    @Override
    public List<UserRespone> findAll() {
        return userRepository.findAll()
                .stream()
                .map(user -> modelMapper.map(user, UserRespone.class))
                .collect(Collectors.toList());
    }
    public User findbyemail(String em) {
        return null;
    }
}
