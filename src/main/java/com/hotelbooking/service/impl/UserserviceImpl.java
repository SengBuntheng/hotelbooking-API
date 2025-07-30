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
        if (userRepository.findByEmail(userRequest.getEmail()).isPresent()) {
            throw new GlobalExceptionHandler.DuplicateResourceException("An account with this email already exists.");
        }

        userHandlerService.validateUserRequest(userRequest);

        User user = new User();
        user.setFirstName(userRequest.getFirstName());
        user.setLastName(userRequest.getLastName());
        user.setEmail(userRequest.getEmail());
        user.setPhone(userRequest.getPhone());
        user.setPasswordHash(passwordEncoder.encode(userRequest.getPassword()));

        user.setActive(false);


        // Save the user to the database
        User savedUser = userRepository.save(user);
        log.info("Created inactive user: {}", savedUser.getEmail());

        String token = jwtUtils.generateToken(savedUser.getEmail());


        UserRespone response = modelMapper.map(savedUser, UserRespone.class);
        response.setToken(token);
        return response;
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
