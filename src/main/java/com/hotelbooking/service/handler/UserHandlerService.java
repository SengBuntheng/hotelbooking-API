package com.hotelbooking.service.handler;

import ch.qos.logback.core.util.StringUtil;
import com.hotelbooking.GlobalException.GlobalExceptionHandler;
import com.hotelbooking.Repository.UserRepository;
import com.hotelbooking.dto.UserRequest;
import com.hotelbooking.dto.UserRespone;
import com.hotelbooking.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;

@Service
@Slf4j
public class UserHandlerService {

    public UserHandlerService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Autowired
    UserRepository userRepository;

    public void validateUserRequest(UserRequest userRequest) {
        if (!StringUtils.hasText(userRequest.getEmail())) {
            throw new IllegalArgumentException("Email cannot be empty.");
        }
        if (!StringUtils.hasText(userRequest.getPhone())) {
            throw new IllegalArgumentException("Phone number cannot be empty.");
        }

    }
    private static boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) return false;

        boolean hasLetter = password.chars().anyMatch(Character::isLetter);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);

        return hasLetter && hasDigit;
    }
    public void validationPasswrod(String password) {

        if (password.isEmpty()) {
            log.error("Password cannot be empty");
        }
    }
    public UserRespone ConvertUserToUserResponse(User user) {
        UserRespone userResponse = new UserRespone();
        userResponse.setId(user.getId());
        userResponse.setEmail(user.getEmail());
        userResponse.setPhone(user.getPhone());
        userResponse.setUsername(user.getUsername());
        userResponse.setFirstName(user.getFirstName());
        userResponse.setLastName(user.getLastName());
        userResponse.setActive(user.getActive());
        userResponse.setToken(user.getToken());
        userResponse.setLastLogin(new Timestamp(System.currentTimeMillis()));
        userResponse.setRole(String.valueOf(user.getRole()));
        userResponse.setTokenExp(user.getExpDate());
        userResponse.setUuid(user.getUuid());

        // CORRECTED: Add null checks before converting to Timestamp
        if (user.getLastLogin() != null) {
            userResponse.setLastLogin(Timestamp.valueOf(user.getLastLogin()));
        }
        if (user.getUpdatedAt() != null) {
            userResponse.setUpdatedAt(Timestamp.valueOf(user.getUpdatedAt()));
        }
        if (user.getCreatedAt() != null) {
            userResponse.setCreatedAt(Timestamp.valueOf(user.getCreatedAt()));
        }

        userResponse.setUpdatedBy(user.getUpdatedBy());
        userResponse.setCreatedBy(user.getCreatedBy());

        return userResponse;
    }



}
