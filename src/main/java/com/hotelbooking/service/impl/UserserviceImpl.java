package com.hotelbooking.service.impl;

import com.hotelbooking.Constant.Constant;
import com.hotelbooking.Enum.UserRole;
import com.hotelbooking.Repository.UserRepository;
import com.hotelbooking.dto.UserRequest;
import com.hotelbooking.dto.UserRespone;
import com.hotelbooking.model.User;
import com.hotelbooking.service.UserService;
import com.hotelbooking.service.handler.UserHandlerService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserserviceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserHandlerService userHandlerService;
    private final ModelMapper modelMapper;

    public UserserviceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, UserHandlerService userHandlerService, ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userHandlerService = userHandlerService;
        this.modelMapper = modelMapper;
    }

    @Transactional
    @Override
    public UserRespone Create(UserRequest userRequest) {
        userHandlerService.validateUserRequest(userRequest);
        userHandlerService.validationPasswrod(userRequest.getPassword());
        if (userRepository.existsByEmail(userRequest.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }
        if (userRepository.existsByUsername(userRequest.getUsername())) {
            throw new IllegalArgumentException("Username already in use");
        }

        User user = modelMapper.map(userRequest, User.class);
        user.setPasswordHash(passwordEncoder.encode(userRequest.getPassword()));
        user.setUuid(UUID.randomUUID());
        user.setRole(UserRole.ADMIN);
        user.setCreatedBy(Constant.SYSTEM);
        user.setActive(false);

        User savedUser = userRepository.save(user);

        return userHandlerService.ConvertUserToUserResponse(savedUser);
    }

    @Override
    public void activateUser(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            user.setActive(true);
            userRepository.save(user);
        });
    }

    @Override
    public UserRespone Update(Long id, UserRequest userRequest) {
        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("User not found with id: " + id);
        }

        User userUpdated = userOptional.get();
        userUpdated.setFirstName(userRequest.getFirstName());
        userUpdated.setLastName(userRequest.getLastName());
        userUpdated.setPhone(userRequest.getPhone());
        userUpdated.setUpdatedAt(LocalDateTime.now());
        userUpdated.setUpdatedBy(Constant.SYSTEM);

        // Only update the password if a new one is provided
        if (userRequest.getPassword() != null && !userRequest.getPassword().isEmpty()) {
            userUpdated.setPasswordHash(passwordEncoder.encode(userRequest.getPassword()));
        }

        User savedUser = userRepository.save(userUpdated);
        log.info("User with id {} updated successfully", id);

        return userHandlerService.ConvertUserToUserResponse(savedUser);
    }

    @Override
    public UserRespone Delete(User user) {
        userRepository.delete(user);
        return userHandlerService.ConvertUserToUserResponse(user);
    }

    @Override
    public UserRespone findbyid(Long id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) {
            throw new IllegalArgumentException("User not found with id: " + id);
        }
        return userHandlerService.ConvertUserToUserResponse(user.get());
    }

    @Override
    public List<UserRespone> findAll() {
        return userRepository.findAll()
                .stream()
                .map(userHandlerService::ConvertUserToUserResponse)
                .collect(Collectors.toList());
    }

    @Override
    public boolean userExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    public User findbyemail(String em) {
        return userRepository.findByEmail(em).orElse(null);
    }
}