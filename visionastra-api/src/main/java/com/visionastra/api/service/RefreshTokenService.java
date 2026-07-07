package com.visionastra.api.service;

import com.visionastra.api.dto.RefreshTokenResponse;
import com.visionastra.api.model.RefreshToken;
import com.visionastra.api.model.Sesion;
import com.visionastra.api.model.Usuario;
import com.visionastra.api.repository.RefreshTokenRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

@Service
public class RefreshTokenService {

    private static final int REFRESH_TOKEN_EXPIRATION_DAYS = 1;

    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuditoriaLoginService auditoriaLoginService;
    private final SesionService sesionService;

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuditoriaLoginService auditoriaLoginService,
            SesionService sesionService
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.auditoriaLoginService = auditoriaLoginService;
        this.sesionService = sesionService;
    }

    // 🔥 CREAR REFRESH TOKEN
    public String createRefreshToken(Usuario usuario, Integer idSesion) {

        String rawRefreshToken = generateSecureToken();
        String hashedRefreshToken = passwordEncoder.encode(rawRefreshToken);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUsuario(usuario);
        refreshToken.setTokenHash(hashedRefreshToken);
        refreshToken.setFechaEmision(LocalDateTime.now());
        refreshToken.setFechaExpiracion(LocalDateTime.now().plusDays(REFRESH_TOKEN_EXPIRATION_DAYS));
        refreshToken.setRevocado(false);
        refreshToken.setIdSesion(idSesion);

        refreshTokenRepository.save(refreshToken);

        return rawRefreshToken;
    }

    // 🔥 REFRESH FINAL (MISMA SESIÓN + VALIDACIÓN REAL)
    public RefreshTokenResponse refreshAccessToken(String rawRefreshToken, String ipAddress, String userAgent) {

        List<RefreshToken> activeTokens = refreshTokenRepository.findByRevocadoFalse();

        for (RefreshToken storedToken : activeTokens) {

            boolean matches = passwordEncoder.matches(rawRefreshToken, storedToken.getTokenHash());

            if (matches) {

                // 🔴 VALIDAR TOKEN
                if (Boolean.TRUE.equals(storedToken.getRevocado())) {
                    throw new RuntimeException("El refresh token fue revocado");
                }

                if (storedToken.getFechaExpiracion().isBefore(LocalDateTime.now())) {
                    storedToken.setRevocado(true);
                    refreshTokenRepository.save(storedToken);
                    throw new RuntimeException("El refresh token ha expirado");
                }

                Usuario usuario = storedToken.getUsuario();

                if (usuario == null) {
                    throw new RuntimeException("Token sin usuario");
                }

                if (!"activo".equalsIgnoreCase(usuario.getEstado())) {
                    throw new RuntimeException("Usuario no activo");
                }

                Integer idSesion = storedToken.getIdSesion();

                if (idSesion == null) {
                    throw new RuntimeException("Refresh token sin sesión asociada");
                }

                // 🔥 VALIDAR SESIÓN (USANDO TU SERVICE)
                Sesion sesion = sesionService.obtenerSesionActiva(idSesion);

                if (sesion == null) {
                    throw new RuntimeException("Sesión no activa o no encontrada");
                }

                // 🔥 VALIDAR EXPIRACIÓN
                if (sesion.getFechaExpiracion().isBefore(LocalDateTime.now())) {
                    sesionService.marcarSesionComoExpirada(idSesion);
                    throw new RuntimeException("La sesión ha expirado");
                }

                // 🔥 ROTACIÓN SEGURA
                storedToken.setRevocado(true);
                refreshTokenRepository.save(storedToken);


                // 🔥 NUEVO JWT (MISMA SESIÓN)
                String newAccessToken = jwtService.generateToken(usuario, idSesion);

                // 🔥 NUEVO REFRESH TOKEN
                String newRefreshToken = createRefreshToken(usuario, idSesion);

                auditoriaLoginService.registrarRefreshExitoso(usuario, ipAddress, userAgent);

                return new RefreshTokenResponse(
                        newAccessToken,
                        newRefreshToken,
                        "Bearer"
                );
            }
        }

        throw new RuntimeException("Refresh token inválido");
    }

    private String generateSecureToken() {
        byte[] randomBytes = new byte[36];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}