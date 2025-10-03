package com.hotelbooking.service.handler;

import com.hotelbooking.Enum.UserRole;
import com.hotelbooking.Repository.UserRepository;
import com.hotelbooking.dto.UserRequest;
import com.hotelbooking.dto.UserResponse;
import com.hotelbooking.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserHandlerService {

    private final UserRepository userRepository;
    private final AuthHandlerService authHandlerService;

    public void validateUserRequest(UserRequest userRequest) {
        if (userRequest == null) {
            throw new IllegalArgumentException("User request cannot be null");
        }

        if (!StringUtils.hasText(userRequest.getEmail())) {
            throw new IllegalArgumentException("Email cannot be empty");
        }

        if (!StringUtils.hasText(userRequest.getFirstName())) {
            throw new IllegalArgumentException("First name cannot be empty");
        }

        if (!StringUtils.hasText(userRequest.getLastName())) {
            throw new IllegalArgumentException("Last name cannot be empty");
        }

        if (!StringUtils.hasText(userRequest.getPhone())) {
            throw new IllegalArgumentException("Phone number cannot be empty");
        }

        if (!authHandlerService.isValidEmail(userRequest.getEmail())) {
            throw new IllegalArgumentException("Invalid email format");
        }

        if (!authHandlerService.isValidPhone(userRequest.getPhone())) {
            throw new IllegalArgumentException("Invalid phone number format");
        }

        if (userRequest.getPassword() != null && !authHandlerService.isValidPassword(userRequest.getPassword())) {
            throw new IllegalArgumentException("Password must be at least 8 characters and contain letters, digits, and special characters");
        }

        // Check for duplicate email
        if (authHandlerService.isEmailAlreadyExists(userRequest.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Check for duplicate username if provided
        if (StringUtils.hasText(userRequest.getUsername()) &&
                authHandlerService.isUsernameAlreadyExists(userRequest.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
    }

    public void validateUserRequestForUpdate(Long userId, UserRequest userRequest) {
        if (userRequest == null) {
            throw new IllegalArgumentException("User request cannot be null");
        }

        if (StringUtils.hasText(userRequest.getEmail())) {
            if (!authHandlerService.isValidEmail(userRequest.getEmail())) {
                throw new IllegalArgumentException("Invalid email format");
            }

            // Check if email exists for other users
            userRepository.findByEmail(userRequest.getEmail())
                    .ifPresent(existingUser -> {
                        if (!existingUser.getId().equals(userId)) {
                            throw new IllegalArgumentException("Email already exists");
                        }
                    });
        }

        if (StringUtils.hasText(userRequest.getPhone()) &&
                !authHandlerService.isValidPhone(userRequest.getPhone())) {
            throw new IllegalArgumentException("Invalid phone number format");
        }

        if (StringUtils.hasText(userRequest.getPassword()) &&
                !authHandlerService.isValidPassword(userRequest.getPassword())) {
            throw new IllegalArgumentException("Password must be at least 8 characters and contain letters, digits, and special characters");
        }

        if (StringUtils.hasText(userRequest.getUsername())) {
            userRepository.findByUsername(userRequest.getUsername())
                    .ifPresent(existingUser -> {
                        if (!existingUser.getId().equals(userId)) {
                            throw new IllegalArgumentException("Username already exists");
                        }
                    });
        }
    }

    public User convertUserRequestToUser(UserRequest userRequest) {
        validateUserRequest(userRequest);

        User user = new User();
        user.setUuid(UUID.fromString(UUID.randomUUID().toString()));
        user.setEmail(userRequest.getEmail().toLowerCase().trim());
        user.setFirstName(userRequest.getFirstName().trim());
        user.setLastName(userRequest.getLastName().trim());
        user.setPhone(userRequest.getPhone().trim());
        user.setActive(false); // Default to inactive until email verification
        user.setRole(UserRole.USER); // Default role
        user.setCreatedAt(LocalDateTime.now());

        if (StringUtils.hasText(userRequest.getUsername())) {
            user.setUsername(userRequest.getUsername().trim());
        }

        if (StringUtils.hasText(userRequest.getPassword())) {
            user.setPasswordHash(authHandlerService.hashPassword(userRequest.getPassword()));
        }

        return user;
    }

    public UserResponse convertUserToUserResponse(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        return UserResponse.builder()
                .id(user.getId())
                .uuid(String.valueOf(user.getUuid()))
                .email(user.getEmail())
                .phone(user.getPhone())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .active(user.getActive())
                .role(user.getRole() != null ? user.getRole().toString() : null)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .lastLogin(user.getLastLogin())
                .createdBy(user.getCreatedBy())
                .updatedBy(user.getUpdatedBy())
                .build();
    }

    public User updateUserFromRequest(User existingUser, UserRequest userRequest, String updatedBy) {
        validateUserRequestForUpdate(existingUser.getId(), userRequest);

        if (StringUtils.hasText(userRequest.getEmail())) {
            existingUser.setEmail(userRequest.getEmail().toLowerCase().trim());
        }

        if (StringUtils.hasText(userRequest.getFirstName())) {
            existingUser.setFirstName(userRequest.getFirstName().trim());
        }

        if (StringUtils.hasText(userRequest.getLastName())) {
            existingUser.setLastName(userRequest.getLastName().trim());
        }

        if (StringUtils.hasText(userRequest.getPhone())) {
            existingUser.setPhone(userRequest.getPhone().trim());
        }

        if (StringUtils.hasText(userRequest.getUsername())) {
            existingUser.setUsername(userRequest.getUsername().trim());
        }

        if (StringUtils.hasText(userRequest.getPassword())) {
            existingUser.setPasswordHash(authHandlerService.hashPassword(userRequest.getPassword()));
        }

        existingUser.setUpdatedAt(LocalDateTime.now());
        existingUser.setUpdatedBy(updatedBy);

        return existingUser;
    }

    public void logUserActivity(String action, User user, String details) {
        log.info("User Activity - Action: {}, User: {} ({}), Details: {}",
                action, user.getEmail(), user.getId(), details);
    }

    public boolean canUserBeDeleted(User user) {

        return user != null && !user.getActive();
    }
}
