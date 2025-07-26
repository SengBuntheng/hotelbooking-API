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
    UserRespone Create (UserRequest userRequest);
    UserRespone Update (User user);
    UserRespone Delete (User user);
    UserRespone findbyid (Long id);
    List<UserRespone> findAll();
    boolean userExists(String email);
    void activateUser(String email);
}
