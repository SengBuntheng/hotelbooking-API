package com.hotelbooking.service;

import com.hotelbooking.dto.LoginRequest;
import com.hotelbooking.dto.LoginResponse;

public interface AuthService    {
    LoginResponse login(LoginRequest loginRequest);
    LoginResponse loginWithOtp(String email, String otp);
}

