package com.visionastra.api.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InternalAdminKeyFilterTest {

    @AfterEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void claveInternaAusenteResponde403() throws Exception {
        ResultadoFiltro resultado = ejecutarFiltro("clave-configurada", null);

        assertEquals(403, resultado.response.getStatus());
        assertEquals(false, resultado.chainInvocado.get());
    }

    @Test
    void claveInternaVaciaResponde403() throws Exception {
        ResultadoFiltro resultado = ejecutarFiltro("clave-configurada", "   ");

        assertEquals(403, resultado.response.getStatus());
        assertEquals(false, resultado.chainInvocado.get());
    }

    @Test
    void claveInternaConfiguradaVaciaResponde403() throws Exception {
        ResultadoFiltro resultado = ejecutarFiltro("", "clave-enviada");

        assertEquals(403, resultado.response.getStatus());
        assertEquals(false, resultado.chainInvocado.get());
    }

    @Test
    void claveInternaIncorrectaResponde403() throws Exception {
        ResultadoFiltro resultado = ejecutarFiltro("clave-configurada", "clave-incorrecta");

        assertEquals(403, resultado.response.getStatus());
        assertEquals(false, resultado.chainInvocado.get());
    }

    @Test
    void claveInternaCorrectaPermiteLlegarAlEndpoint() throws Exception {
        ResultadoFiltro resultado = ejecutarFiltro("clave-configurada", "clave-configurada");

        assertEquals(200, resultado.response.getStatus());
        assertTrue(resultado.chainInvocado.get());
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertTrue(SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities()
                .stream()
                .anyMatch(authority -> "ROLE_INTERNAL_ADMIN".equals(authority.getAuthority())));
    }

    private ResultadoFiltro ejecutarFiltro(String configuredKey, String providedKey) throws ServletException, IOException {
        InternalAdminKeyFilter filter = new InternalAdminKeyFilter(configuredKey);

        MockHttpServletRequest request = new MockHttpServletRequest("PATCH", "/api/internal/admin/usuarios/3/bloquear");
        request.setServletPath("/api/internal/admin/usuarios/3/bloquear");

        if (providedKey != null) {
            request.addHeader("X-Internal-Admin-Key", providedKey);
        }

        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean chainInvocado = new AtomicBoolean(false);

        FilterChain chain = new FilterChain() {
            @Override
            public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse) {
                chainInvocado.set(true);
            }
        };

        filter.doFilter(request, response, chain);

        return new ResultadoFiltro(response, chainInvocado);
    }

    private record ResultadoFiltro(MockHttpServletResponse response, AtomicBoolean chainInvocado) {
    }
}
