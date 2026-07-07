package com.visionastra.api.controller;

import com.visionastra.api.dto.AdminUsuarioEstadoResponse;
import com.visionastra.api.exception.GlobalExceptionHandler;
import com.visionastra.api.exception.UsuarioNoEncontradoException;
import com.visionastra.api.service.AdminUsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminUsuarioControllerTest {

    private AdminUsuarioService adminUsuarioService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        adminUsuarioService = mock(AdminUsuarioService.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new AdminUsuarioController(adminUsuarioService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void usuarioInexistenteAlBloquearResponde404() throws Exception {
        when(adminUsuarioService.bloquearUsuario(99L))
                .thenThrow(new UsuarioNoEncontradoException(99L));

        mockMvc.perform(patch("/api/internal/admin/usuarios/99/bloquear"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void usuarioInexistenteAlActivarResponde404() throws Exception {
        when(adminUsuarioService.activarUsuario(99L))
                .thenThrow(new UsuarioNoEncontradoException(99L));

        mockMvc.perform(patch("/api/internal/admin/usuarios/99/activar"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void claveCorrectaPermiteRespuestaDelEndpoint() throws Exception {
        when(adminUsuarioService.bloquearUsuario(3L))
                .thenReturn(new AdminUsuarioEstadoResponse(
                        3L,
                        "bloqueado",
                        "Usuario bloqueado correctamente."
                ));

        mockMvc.perform(patch("/api/internal/admin/usuarios/3/bloquear"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idUsuario").value(3))
                .andExpect(jsonPath("$.estado").value("bloqueado"))
                .andExpect(jsonPath("$.mensaje").value("Usuario bloqueado correctamente."));
    }

    @Test
    void getEnEndpointDeBloqueoEsRechazadoPorMetodo() throws Exception {
        mockMvc.perform(get("/api/internal/admin/usuarios/3/bloquear"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void postEnEndpointDeActivacionEsRechazadoPorMetodo() throws Exception {
        mockMvc.perform(post("/api/internal/admin/usuarios/3/activar"))
                .andExpect(status().isMethodNotAllowed());
    }
}
