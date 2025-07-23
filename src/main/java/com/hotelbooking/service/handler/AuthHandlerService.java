package com.hotelbooking.service.handler;

import com.hotelbooking.Repository.UserRepository;
import com.hotelbooking.dto.UserRespone;
import com.hotelbooking.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AuthHandlerService {


    public AuthHandlerService(UserRepository userRepository, PasswordEncoder passwordEncoder) {

    }
    public static UserRespone mapToUserResponse(User user) {
        UserRespone response = new UserRespone();
        response.setUuid(user.getUuid());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        return response;
    }

}
