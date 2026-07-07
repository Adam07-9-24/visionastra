package com.visionastra.api.service.impl;

import com.visionastra.api.dto.AdminUsuarioEstadoResponse;
import com.visionastra.api.exception.UsuarioNoEncontradoException;
import com.visionastra.api.model.RefreshToken;
import com.visionastra.api.model.Sesion;
import com.visionastra.api.model.Usuario;
import com.visionastra.api.repository.RefreshTokenRepository;
import com.visionastra.api.repository.SesionRepository;
import com.visionastra.api.repository.UsuarioRepository;
import com.visionastra.api.service.SesionWebSocketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminUsuarioServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private SesionRepository sesionRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private SesionWebSocketService sesionWebSocketService;

    private AdminUsuarioServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AdminUsuarioServiceImpl(
                usuarioRepository,
                sesionRepository,
                refreshTokenRepository,
                sesionWebSocketService
        );
    }

    @Test
    void bloquearUsuarioInexistenteLanza404() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(UsuarioNoEncontradoException.class, () -> service.bloquearUsuario(99L));
    }

    @Test
    void activarUsuarioInexistenteLanza404() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(UsuarioNoEncontradoException.class, () -> service.activarUsuario(99L));
    }

    @Test
    void bloquearUsuarioActivoCierraSesionesYRevocaTokens() {
        Usuario usuario = usuario(3L, "activo");
        Sesion sesion1 = sesion(10);
        Sesion sesion2 = sesion(11);
        RefreshToken token1 = refreshToken(false);
        RefreshToken token2 = refreshToken(null);

        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(usuario));
        when(sesionRepository.findByUsuarioAndEstado(usuario, "activa")).thenReturn(List.of(sesion1, sesion2));
        when(refreshTokenRepository.findByUsuario(usuario)).thenReturn(List.of(token1, token2));

        AdminUsuarioEstadoResponse response = service.bloquearUsuario(3L);

        assertEquals("bloqueado", usuario.getEstado());
        assertEquals("cerrada", sesion1.getEstado());
        assertEquals("cerrada", sesion2.getEstado());
        assertEquals(true, token1.getRevocado());
        assertEquals(true, token2.getRevocado());
        assertEquals(3L, response.getIdUsuario());
        assertEquals("bloqueado", response.getEstado());

        verify(sesionRepository).saveAll(List.of(sesion1, sesion2));
        verify(refreshTokenRepository).saveAll(List.of(token1, token2));
        verify(usuarioRepository).save(usuario);
        verify(sesionWebSocketService, times(2)).notificarCambioSesion(any(), any());
    }

    @Test
    void bloquearUsuarioYaBloqueadoSigueInvalidandoPendientes() {
        Usuario usuario = usuario(3L, "bloqueado");
        Sesion sesion = sesion(10);
        RefreshToken token = refreshToken(false);

        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(usuario));
        when(sesionRepository.findByUsuarioAndEstado(usuario, "activa")).thenReturn(List.of(sesion));
        when(refreshTokenRepository.findByUsuario(usuario)).thenReturn(List.of(token));

        AdminUsuarioEstadoResponse response = service.bloquearUsuario(3L);

        assertEquals("bloqueado", usuario.getEstado());
        assertEquals("cerrada", sesion.getEstado());
        assertEquals(true, token.getRevocado());
        assertEquals("bloqueado", response.getEstado());
    }

    @Test
    void activarUsuarioBloqueadoCambiaEstadoAActivo() {
        Usuario usuario = usuario(3L, "bloqueado");
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(usuario));

        AdminUsuarioEstadoResponse response = service.activarUsuario(3L);

        assertEquals("activo", usuario.getEstado());
        assertEquals("activo", response.getEstado());
        verify(usuarioRepository).save(usuario);
        verifyNoInteractions(sesionRepository, refreshTokenRepository, sesionWebSocketService);
    }

    @Test
    void activarUsuarioYaActivoDevuelve200SinTocarSesionesNiTokens() {
        Usuario usuario = usuario(3L, "activo");
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(usuario));

        AdminUsuarioEstadoResponse response = service.activarUsuario(3L);

        assertEquals("activo", usuario.getEstado());
        assertEquals("activo", response.getEstado());
        verify(usuarioRepository).save(usuario);
        verifyNoInteractions(sesionRepository, refreshTokenRepository, sesionWebSocketService);
    }

    @Test
    void activarNoReabreSesionesNiDesrevocaTokens() {
        Usuario usuario = usuario(3L, "bloqueado");
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(usuario));

        service.activarUsuario(3L);

        verify(sesionRepository, never()).saveAll(any());
        verify(refreshTokenRepository, never()).saveAll(any());
    }

    private Usuario usuario(Long idUsuario, String estado) {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(idUsuario);
        usuario.setEstado(estado);
        return usuario;
    }

    private Sesion sesion(Integer idSesion) {
        Sesion sesion = new Sesion();
        sesion.setIdSesion(idSesion);
        sesion.setEstado("activa");
        return sesion;
    }

    private RefreshToken refreshToken(Boolean revocado) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setRevocado(revocado);
        return refreshToken;
    }
}
