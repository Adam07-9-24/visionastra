package com.visionastra.api.controller;

import com.visionastra.api.dto.LoginRequest;
import com.visionastra.api.dto.LoginResponse;
import com.visionastra.api.dto.RefreshTokenRequest;
import com.visionastra.api.dto.RefreshTokenResponse;
import com.visionastra.api.dto.RegisterRequest;
import com.visionastra.api.dto.RegisterResponse;
import com.visionastra.api.service.AuthService;
import com.visionastra.api.service.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;

    public AuthController(
            AuthService authService,
            RefreshTokenService refreshTokenService
    ) {
        this.authService = authService;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
            @RequestBody RegisterRequest request
    ) {
        RegisterResponse response = authService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    //  LOGIN
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @RequestBody LoginRequest request,
            HttpServletRequest httpRequest
    ) {

        String ipAddress = httpRequest.getRemoteAddr();
        String userAgent = httpRequest.getHeader("User-Agent");

        LoginResponse response = authService.login(request, ipAddress, userAgent);

        return ResponseEntity.ok(response);
    }

    // REFRESH TOKEN
    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenResponse> refreshToken(
            @RequestBody RefreshTokenRequest request,
            HttpServletRequest httpRequest
    ) {

        String ipAddress = httpRequest.getRemoteAddr();
        String userAgent = httpRequest.getHeader("User-Agent");

        RefreshTokenResponse response = refreshTokenService.refreshAccessToken(
                request.getRefreshToken(),
                ipAddress,
                userAgent
        );

        return ResponseEntity.ok(response);
    }

    //  LOGOUT PRO
    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            HttpServletRequest request
    ) {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Token no enviado");
        }

        String token = authHeader.substring(7);

        String ipAddress = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");

        authService.logout(token, ipAddress, userAgent);

        return ResponseEntity.ok("Sesión cerrada solo en este dispositivo");
    }
}
