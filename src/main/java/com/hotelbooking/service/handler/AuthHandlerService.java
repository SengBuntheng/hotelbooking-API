package com.hotelbooking.service.handler;

import com.hotelbooking.Repository.UserRepository;
import com.hotelbooking.dto.UserResponse;
import com.hotelbooking.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthHandlerService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^[+]?[0-9]{8,15}$"
    );

    public UserResponse mapToUserResponse(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        return UserResponse.builder()
                .id(user.getId())
                .uuid(String.valueOf(user.getUuid()))
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .username(user.getUsername())
                .active(user.getActive())
                .role(user.getRole() != null ? user.getRole().toString() : null)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .lastLogin(user.getLastLogin())
                .createdBy(user.getCreatedBy())
                .updatedBy(user.getUpdatedBy())
                .build();
    }

    public boolean isValidPassword(String password) {
        if (password == null || password.length() < 8 || password.length() > 128) {
            return false;
        }

        boolean hasLetter = password.chars().anyMatch(Character::isLetter);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        boolean hasSpecialChar = password.chars().anyMatch(ch -> "!@#$%^&*()_+-=[]{}|;:,.<>?".indexOf(ch) >= 0);

        return hasLetter && hasDigit && hasSpecialChar;
    }

    public boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }

    public String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        return passwordEncoder.encode(plainPassword);
    }

    public boolean verifyPassword(String plainPassword, String hashedPassword) {
        return passwordEncoder.matches(plainPassword, hashedPassword);
    }

    public void validateUserCredentials(String email, String password) {
        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid email format");
        }

        if (!isValidPassword(password)) {
            throw new IllegalArgumentException("Password must be at least 8 characters long and contain letters, digits, and special characters");
        }
    }

    public boolean isEmailAlreadyExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    public boolean isUsernameAlreadyExists(String username) {
        return userRepository.findByUsername(username).isPresent();
    }
}