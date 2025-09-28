package com.hotelbooking.service;


import com.hotelbooking.dto.UserRequest;
import com.hotelbooking.dto.UserResponse;
import com.hotelbooking.model.User;
import jakarta.persistence.PrePersist;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public interface UserService {
    UserResponse Create(UserRequest userRequest);
    UserResponse Update(Long id , UserRequest userRequest);
    UserResponse Delete(User user);
    UserResponse findbyid(Long id);
    List<UserResponse> findAll();

    void deleteUser(Long id);
    UserResponse findByEmail(String email);

    void activateUser(String email);

    boolean userExists(String email);
}
