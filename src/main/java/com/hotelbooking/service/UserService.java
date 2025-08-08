package com.hotelbooking.service;


import com.hotelbooking.dto.UserRequest;
import com.hotelbooking.dto.UserRespone;
import com.hotelbooking.model.User;
import jakarta.persistence.PrePersist;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public interface UserService {
    UserRespone Create(UserRequest userRequest);
    UserRespone Update(Long id , UserRequest userRequest);
    UserRespone Delete(User user);
    UserRespone findbyid(Long id);
    List<UserRespone> findAll();

    // ADDED: Activates a user's account after OTP verification.
    void activateUser(String email);

    // ADDED: Checks if a user exists by their email.
    boolean userExists(String email);
}
