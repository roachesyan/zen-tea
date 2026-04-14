package com.zentea.service;

import com.zentea.dto.request.LoginRequest;
import com.zentea.dto.request.RegisterRequest;
import com.zentea.dto.response.LoginResponse;

public interface AuthService {

    LoginResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    LoginResponse refreshToken(String refreshToken);

    void logout(Long userId);
}
