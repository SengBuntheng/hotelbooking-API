package com.hotelbooking.service.impl;

import com.hotelbooking.Config.JwtService;
import com.hotelbooking.Enum.VerificationResult;
import com.hotelbooking.GlobalException.OtpException;
import com.hotelbooking.Repository.UserRepository;
import com.hotelbooking.dto.AuthenticationRequest;
import com.hotelbooking.dto.LoginResponse;
import com.hotelbooking.dto.UserRespone;
import com.hotelbooking.model.User;
import com.hotelbooking.service.AuthService;
import com.hotelbooking.service.EmailOtpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final EmailOtpService emailOtpService;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(AuthenticationRequest authenticationRequest) {
        try {
            // Perform authentication using Spring Security
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authenticationRequest.getEmail(),
                            authenticationRequest.getPassword()
                    )
            );

            // Set authenticated user in the security context
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Load user from DB (ideally by username/email)
            User user = userRepository.findByEmail(authenticationRequest.getEmail())
                    .orElseThrow(() -> new OtpException.UserNotFoundException(
                            "User not found with email: " + authenticationRequest.getEmail()
                    ));

            if (!user.getActive()) {
                throw new OtpException.AuthenticationFailedException("Account is inactive");
            }

            // Generate JWT token
            String token = jwtService.generateToken(user.getEmail());

            // Update last login timestamp
            updateLastLogin(user);

            // Return the response
            return buildLoginResponse(user, token);

        } catch (BadCredentialsException e) {
            log.warn("Invalid credentials for email: {}", authenticationRequest.getEmail());
            throw new OtpException.AuthenticationFailedException("Invalid email or password");
        } catch (OtpException e) {
            // Already a known custom exception
            throw e;
        } catch (Exception e) {
            log.error("Authentication failed for email: {}", authenticationRequest.getEmail(), e);
            throw new OtpException.AuthenticationFailedException("Authentication failed");
        }
    }


    @Override
    @Transactional
    public LoginResponse loginWithOtp(String email, String otp) {
        try {
            VerificationResult result = emailOtpService.verifyOtp(email, otp);

            if (!result.isValid()) {
                throw new OtpException.OtpVerificationException("OTP verification failed: " + result.getStatus());
            }

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new OtpException.UserNotFoundException("User not found with email: " + email));

            if (!user.getActive()) {
                throw new OtpException.AuthenticationFailedException("Account is inactive");
            }

            String token = jwtService.generateToken(email);
            updateLastLogin(user);

            LoginResponse response = buildLoginResponse(user, token);
            log.info("OTP login successful for user: {}", email);
            return response;

        } catch (OtpException.OtpVerificationException e) {
            log.warn("OTP verification failed for email: {}", email);
            throw e;
        } catch (Exception e) {
            log.error("OTP login failed for email: {}", email, e);
            throw new OtpException.AuthenticationFailedException("OTP login failed");
        }
    }

    private void updateLastLogin(User user) {
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
    }

    private LoginResponse buildLoginResponse(User user, String token) {
        Objects.requireNonNull(user, "User cannot be null");
        return LoginResponse.builder()
                .token(token)
                .user(mapToUserResponse(user))
                .build();
    }

    private UserRespone mapToUserResponse(User user) {
        return UserRespone.builder()
                .uuid(user.getUuid())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .createdAt(user.getCreateDate())
                .build();
    }
}