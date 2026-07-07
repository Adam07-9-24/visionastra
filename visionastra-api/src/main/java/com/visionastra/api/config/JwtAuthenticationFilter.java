package com.visionastra.api.config;

import com.visionastra.api.model.Sesion;
import com.visionastra.api.model.Usuario;
import com.visionastra.api.repository.SesionRepository;
import com.visionastra.api.repository.UsuarioRepository;
import com.visionastra.api.service.JwtService;
import com.visionastra.api.service.SesionService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;
    private final SesionRepository sesionRepository;
    private final SesionService sesionService;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UsuarioRepository usuarioRepository,
            SesionRepository sesionRepository,
            SesionService sesionService
    ) {
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
        this.sesionRepository = sesionRepository;
        this.sesionService = sesionService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // 🔹 No hay token → dejar pasar endpoints públicos
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = authHeader.substring(7);
        String email;

        try {
            email = jwtService.extractUsername(jwt);
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token inválido");
            return;
        }

        // 🔹 Evitar re-autenticación
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // 🔹 Validar JWT básico
            if (!jwtService.isTokenValid(jwt, email)) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token expirado o inválido");
                return;
            }

            // 🔹 Buscar usuario
            Usuario usuario = usuarioRepository.findByEmail(email)
                    .orElse(null);

            if (usuario == null) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Usuario no válido");
                return;
            }

            // 🔥 Extraer idSesion del JWT
            Integer idSesion = jwtService.extractIdSesion(jwt);

            if (idSesion == null) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token sin sesión");
                return;
            }

            Sesion sesion = sesionRepository.findById(idSesion).orElse(null);

            if (sesion == null) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Sesión no encontrada");
                return;
            }

            // 🔥 1. VALIDAR EXPIRACIÓN
            // IMPORTANTE:
            // No marcar directamente con sesionRepository.save(...)
            // porque eso no revoca refresh tokens.
            // Usamos SesionService para centralizar la lógica.
            if (sesion.getFechaExpiracion().isBefore(LocalDateTime.now())) {
                sesionService.marcarSesionComoExpirada(idSesion);

                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Sesión expirada");
                return;
            }

            // 🔥 2. VALIDAR ESTADO
            if (!"activa".equalsIgnoreCase(sesion.getEstado())) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Sesión inválida");
                return;
            }

            // 🔐 AUTENTICAR USUARIO
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            email,
                            null,
                            Collections.emptyList()
                    );

            authToken.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
            );

            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        filterChain.doFilter(request, response);
    }
}