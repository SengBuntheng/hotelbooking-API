package com.hotelbooking.service.handler;

import ch.qos.logback.core.util.StringUtil;
import com.hotelbooking.GlobalException.GlobalExceptionHandler;
import com.hotelbooking.Repository.UserRepository;
import com.hotelbooking.dto.UserRequest;
import com.hotelbooking.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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
}
