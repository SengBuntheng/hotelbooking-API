package com.hotelbooking.service;

import com.hotelbooking.dto.AuthenticationRequest;
import com.hotelbooking.dto.AuthenticationResponse;
import com.hotelbooking.dto.LoginResponse;

public interface AuthService {
    LoginResponse login(AuthenticationRequest authenticationRequest);
    LoginResponse loginWithOtp(String email, String otp);
    AuthenticationResponse refreshToken(String refreshToken);
}
