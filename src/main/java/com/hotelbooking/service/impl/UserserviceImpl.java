package com.hotelbooking.service.impl;

import com.hotelbooking.Constant.Constant;
import com.hotelbooking.Repository.UserRepository;
import com.hotelbooking.dto.UserRequest;
import com.hotelbooking.dto.UserResponse;
import com.hotelbooking.model.User;
import com.hotelbooking.service.UserService;
import com.hotelbooking.service.handler.UserHandlerService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserserviceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserHandlerService userHandlerService;
    private final ModelMapper modelMapper;

    public UserserviceImpl(UserRepository userRepository, UserHandlerService userHandlerService, ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.userHandlerService = userHandlerService;
        this.modelMapper = modelMapper;
    }

    @Transactional
    @Override
    public UserResponse Create(UserRequest userRequest) {
        userHandlerService.validateUserRequest(userRequest);
        User user = userHandlerService.convertUserRequestToUser(userRequest);
        User savedUser = userRepository.save(user);
        log.info("User created with ID {}", savedUser.getId());
        return userHandlerService.convertUserToUserResponse(savedUser);
    }

    @Override
    public void activateUser(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            user.setActive(true);
            userRepository.save(user);
            log.info("Activated user with email {}", email);
        });
    }

    @Override
    public UserResponse Update(Long id, UserRequest userRequest) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty()) {
            throw new IllegalArgumentException("User not found with ID: " + id);
        }

        User updatedUser = userHandlerService.updateUserFromRequest(optionalUser.get(), userRequest, Constant.SYSTEM);
        User savedUser = userRepository.save(updatedUser);
        log.info("Updated user with ID {}", id);
        return userHandlerService.convertUserToUserResponse(savedUser);
    }

    @Override
    public UserResponse Delete(User user) {
        if (!userHandlerService.canUserBeDeleted(user)) {
            throw new IllegalArgumentException("User cannot be deleted due to active status or business rules.");
        }
        userRepository.delete(user);
        log.info("Deleted user with ID {}", user.getId());
        return userHandlerService.convertUserToUserResponse(user);
    }

    @Override
    public UserResponse findbyid(Long id) {
        return userRepository.findById(id)
                .map(userHandlerService::convertUserToUserResponse)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + id));
    }

    @Override
    public List<UserResponse> findAll() {
        return userRepository.findAll()
                .stream()
                .map(userHandlerService::convertUserToUserResponse)
                .collect(Collectors.toList());
    }

    @Override
    public boolean userExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    public User findbyemail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }
    @Override
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
        if (!userHandlerService.canUserBeDeleted(user)) {
            throw new IllegalArgumentException("User cannot be deleted due to active status or business rules.");
        }
        userRepository.delete(user);
        log.info("Deleted user with ID {}", id);
    }

    @Override
    public UserResponse findByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(userHandlerService::convertUserToUserResponse)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));
    }

}
