package com.visionastra.api.service;

import com.visionastra.api.dto.LoginRequest;
import com.visionastra.api.dto.LoginResponse;
import com.visionastra.api.dto.RegisterRequest;
import com.visionastra.api.dto.RegisterResponse;

public interface AuthService {

    RegisterResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request, String ipAddress, String userAgent);

    // 🔥 AGREGA ESTO
    void logout(String token, String ipAddress, String userAgent);
}
