package com.visionastra.api.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

@Component
public class InternalAdminKeyFilter extends OncePerRequestFilter {

    private static final String HEADER_NAME = "X-Internal-Admin-Key";
    private static final String INTERNAL_ADMIN_ROLE = "ROLE_INTERNAL_ADMIN";
    private static final String INTERNAL_ADMIN_PRINCIPAL = "internal-admin";
    private static final String INTERNAL_ADMIN_PATH = "/api/internal/admin/";

    private final String configuredKey;

    public InternalAdminKeyFilter(
            @Value("${visionastra.internal.admin.key:}") String configuredKey
    ) {
        this.configuredKey = configuredKey;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path == null || !path.startsWith(INTERNAL_ADMIN_PATH);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String providedKey = request.getHeader(HEADER_NAME);

        if (!esClaveValida(providedKey)) {
            SecurityContextHolder.clearContext();
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acceso interno denegado");
            return;
        }

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        INTERNAL_ADMIN_PRINCIPAL,
                        null,
                        List.of(new SimpleGrantedAuthority(INTERNAL_ADMIN_ROLE))
                );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }

    private boolean esClaveValida(String providedKey) {
        if (estaVacia(configuredKey) || estaVacia(providedKey)) {
            return false;
        }

        byte[] configuredBytes = configuredKey.getBytes(StandardCharsets.UTF_8);
        byte[] providedBytes = providedKey.getBytes(StandardCharsets.UTF_8);

        return MessageDigest.isEqual(configuredBytes, providedBytes);
    }

    private boolean estaVacia(String value) {
        return value == null || value.trim().isEmpty();
    }
}
