package com.hotelbooking.service.impl;

import com.hotelbooking.Config.JwtService;
import com.hotelbooking.Enum.VerificationResult;
import com.hotelbooking.Repository.UserRepository;
import com.hotelbooking.dto.LoginRequest;
import com.hotelbooking.dto.LoginResponse;
import com.hotelbooking.GlobalException.OtpException;
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
import org.springframework.stereotype.Service;

import static com.hotelbooking.service.handler.AuthHandlerService.mapToUserResponse;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final EmailOtpService emailOtpService;

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            User user = userRepository.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            String token = jwtService.generateToken(user.getEmail());

            LoginResponse response = new LoginResponse();
            response.setToken(token);
            response.setUser(mapToUserResponse(user));
            return response;

        } catch (Exception e) {
            log.error("Authentication failed for user: {}", loginRequest.getEmail(), e);
            throw new BadCredentialsException("Invalid credentials");
        }
    }

    @Override
    public LoginResponse loginWithOtp(String email, String otp) {
        VerificationResult result = emailOtpService.verifyOtp(email, otp);

        if (!result.isValid()) {
            throw new OtpException("OTP verification failed: " + result.getStatus());
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found after OTP verification"));

        String token = jwtService.generateToken(email);

        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUser(mapToUserResponse(user));

        log.info("OTP login successful for user: {}", email);
        return response;
    }
}
