package com.hotelbooking.service.impl;

import com.hotelbooking.Config.JwtService;
import com.hotelbooking.Repository.UserRepository;
import com.hotelbooking.dto.UserRequest;
import com.hotelbooking.dto.UserRespone;
import com.hotelbooking.model.User;
import com.hotelbooking.service.UserService;
import com.hotelbooking.service.handler.UserHandlerService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j

public class UserserviceImpl implements UserService  {

    @Autowired
    UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserHandlerService userHandlerService;
    private final ModelMapper modelMapper;
    private final JwtService jwtUtils;
    private final AuthenticationManager authenticationManager;
    @Autowired

    public UserserviceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           UserHandlerService userHandlerService,
                           ModelMapper modelMapper,
                           JwtService jwtUtils,
                           AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userHandlerService = userHandlerService;
        this.modelMapper = modelMapper;
        this.jwtUtils = jwtUtils;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    @Override
    public UserRespone Create(UserRequest userRequest) {
        userHandlerService.validateUserRequest(userRequest);
        User user = modelMapper.map(userRequest, User.class);
        user.setPasswordHash(passwordEncoder.encode(userRequest.getPassword()));
        user.setUuid(UUID.randomUUID());
        user.setActive(true);
        user.setCreatedBy("SYSTEM");
        String token = jwtUtils.generateToken(user.getEmail());
        User savedUser = userRepository.save(user);
        if(userRepository.existsByEmail(user.getEmail())) {
            throw  new IllegalArgumentException("Email already in use");
        }
        if(userRepository.existsByUsername(userRequest.getUsername())) {
            throw  new IllegalArgumentException("Username already in use");
        }
        userHandlerService.validateUserRequest(userRequest);


        return UserRespone.builder()
                .uuid(savedUser.getUuid())
                .firstName(savedUser.getFirstName())
                .lastName(savedUser.getLastName())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .phone(savedUser.getPhone())
                .createdAt(savedUser.getCreateDate())
                .token(token)
                .tokenExp(new Timestamp(jwtUtils.extractExpiration(token).getTime()))
                .code(200)
                .message("User created successfully")
                .build();
    }


    @Override
    public UserRespone Update(User user) {
        return userRepository.findById(user.getId())
                .map(existingUser -> {
                    existingUser.setFirstName(user.getFirstName());
                    existingUser.setLastName(user.getLastName());
                    existingUser.setPhone(user.getPhone());
                    existingUser.setUpdatedAt(LocalDateTime.now());
                    User updatedUser = userRepository.save(existingUser);
                    return modelMapper.map(updatedUser, UserRespone.class);
                })
                .orElse(null);
    }
    @Override
    public UserRespone Delete(User user) {
        return null;
    }

    @Override
    public UserRespone findbyid(Long id) {
        return userRepository.findById(id)
                .map(user -> modelMapper.map(user, UserRespone.class))
                .orElse(null);

    }

    @Override
    public List<UserRespone> findAll() {
        return userRepository.findAll()
                .stream()
                .map(user -> modelMapper.map(user, UserRespone.class))
                .collect(Collectors.toList());
    }

    @Override
    public boolean userExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    @Override
    public void activateUser(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            user.setActive(true);
            userRepository.save(user);
        });
    }

    public User findbyemail(String em) {
        return userRepository.findByEmail(em).orElse(null);

    };
}
