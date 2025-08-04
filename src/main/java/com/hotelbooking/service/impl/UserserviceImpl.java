package com.hotelbooking.service.impl;

import com.hotelbooking.Config.JwtService;
import com.hotelbooking.GlobalException.GlobalExceptionHandler;
import com.hotelbooking.Repository.UserRepository;
import com.hotelbooking.dto.LoginRequest;
import com.hotelbooking.dto.LoginResponse;
import com.hotelbooking.dto.UserRequest;
import com.hotelbooking.dto.UserRespone;
import com.hotelbooking.model.User;
import com.hotelbooking.service.UserService;
import com.hotelbooking.service.handler.UserHandlerService;
import io.jsonwebtoken.Jwt;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j

public class UserserviceImpl implements UserService  {

    @Autowired
    UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserHandlerService userHandlerService;
    private final ModelMapper modelMapper;
    private final JwtService jwtUtils;
    private final AuthenticationManager authenticationManager;
    @Autowired

    public UserserviceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           UserHandlerService userHandlerService,
                           ModelMapper modelMapper,
                           JwtService jwtUtils,
                           AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userHandlerService = userHandlerService;
        this.modelMapper = modelMapper;
        this.jwtUtils = jwtUtils;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    @Override
    public UserRespone Create(UserRequest userRequest) {
        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            userRequest.getUsername(),
                            userRequest.getPassword()
                    )
            );

            if (!authentication.isAuthenticated()) {
                UserRespone response = new UserRespone();
                response.setCode(HttpStatus.UNAUTHORIZED.value());
                response.setMessage("Login failed.");
                return response;
            }

            // Retrieve user from the database
            Optional<User> optionalUser = userRepository.findByUsername(userRequest.getUsername());
            if (optionalUser.isEmpty()) {
                throw new UsernameNotFoundException("User not found.");
            }

            User user = optionalUser.get();

            // Generate JWT token
            String token = jwtUtils.generateToken(user.getUsername());

            // Build response
            UserRespone response = new UserRespone();
            BeanUtils.copyProperties(user, response);
            response.setToken(token);
            response.setTokenExp(new Timestamp(System.currentTimeMillis() + 600_000)); // 10 minutes
            response.setCode(HttpStatus.OK.value());
            response.setMessage("Login successfully.");

            return response;

        } catch (Exception ex) {
            UserRespone response = new UserRespone();
            response.setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setMessage("Login failed: " + ex.getMessage());
            return response;
        }
    }


    @Override
    public UserRespone Update(User user) {
        return userRepository.findById(user.getId())
                .map(existingUser -> {
                    existingUser.setFirstName(user.getFirstName());
                    existingUser.setLastName(user.getLastName());
                    existingUser.setPhone(user.getPhone());
                    existingUser.setUpdatedAt(LocalDateTime.now());
                    User updatedUser = userRepository.save(existingUser);
                    return modelMapper.map(updatedUser, UserRespone.class);
                })
                .orElse(null);
    }
    @Override
    public UserRespone Delete(User user) {
        return null;
    }

    @Override
    public UserRespone findbyid(Long id) {
        return userRepository.findById(id)
                .map(user -> modelMapper.map(user, UserRespone.class))
                .orElse(null);

    }

    @Override
    public List<UserRespone> findAll() {
        return userRepository.findAll()
                .stream()
                .map(user -> modelMapper.map(user, UserRespone.class))
                .collect(Collectors.toList());
    }

    @Override
    public boolean userExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    @Override
    public void activateUser(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            user.setActive(true);
            userRepository.save(user);
        });
    }

    public User findbyemail(String em) {
        return userRepository.findByEmail(em).orElse(null);

    };
}
