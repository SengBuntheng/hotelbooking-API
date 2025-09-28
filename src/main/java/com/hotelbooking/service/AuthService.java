package com.hotelbooking.service;

import com.hotelbooking.dto.AuthenticationRequest;
import com.hotelbooking.dto.AuthenticationResponse;
import com.hotelbooking.dto.LoginResponse;
import org.apache.http.auth.AuthenticationException;

public interface AuthService {
    LoginResponse login(AuthenticationRequest authenticationRequest) throws AuthenticationException;
    LoginResponse loginWithOtp(String email, String otp) throws AuthenticationException;
    AuthenticationResponse refreshToken(String refreshToken) throws AuthenticationException;
}